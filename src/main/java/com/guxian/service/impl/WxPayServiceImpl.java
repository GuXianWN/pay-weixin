package com.guxian.service.impl;

import com.google.gson.Gson;
import com.guxian.config.WxPayConfig;
import com.guxian.entity.OrderInfo;
import com.guxian.entity.RefundInfo;
import com.guxian.enums.OrderStatus;
import com.guxian.enums.wxpay.WxApiType;
import com.guxian.enums.wxpay.WxNotifyType;
import com.guxian.service.IWxPayService;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 微信支付
 *
 * @author GuXianWN
 * @date 2021/12/19 14:16
 **/
@Service
@Slf4j
public class WxPayServiceImpl implements IWxPayService {
    @Resource
    private WxPayConfig wxPayConfig;
    @Resource
    private CloseableHttpClient wxPayClient;
    @Resource
    private OrderInfoServiceImpl orderInfoService;
    @Resource
    private PaymentInfoServiceImpl paymentInfoService;
    @Resource
    private RefundInfoServiceImpl refundInfoService;
    @Resource
    //无需应答签名
    private CloseableHttpClient wxPayNoSignClient;

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * @param productId 商品编号
     * @return codUrl和订单号
     * @author GuXianWN
     * @date 2021/12/19 15:02
     * 创建订单
     */
    @Override
    public Map<String, Object> nativePay(Long productId) throws Exception {
        //生成订单
        log.info("生成订单");
        //存入数据库
        OrderInfo orderInfo = orderInfoService.createOrderBy(productId);
        if (orderInfo != null && !StringUtils.isEmpty(orderInfo.getCodeUrl())) {
            log.info("订单已存在");
            Map<String, Object> map = new HashMap<>();
            map.put("codeUrl", orderInfo.getCodeUrl());
            map.put("orderNo", orderInfo.getOrderNo());
            return map;
        }
        //调用统一下单api
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat(WxApiType.NATIVE_PAY.getType()));
        // 请求body参数
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<>();
        map.put("appid", wxPayConfig.getAppid());
        map.put("mchid", wxPayConfig.getMchId());
        map.put("description", orderInfo.getTitle());
        map.put("out_trade_no", orderInfo.getOrderNo());
        map.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxNotifyType.NATIVE_NOTIFY.getType()));

        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("total", orderInfo.getTotalFee());
        map.put("amount", amountMap);
        String reqdata = gson.toJson(map);
        log.info("请求参数" + reqdata);

        StringEntity entity = new StringEntity(reqdata, "utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        //完成签名并执行请求
        CloseableHttpResponse response = wxPayClient.execute(httpPost);

        try {
            String body = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                //处理成功
                log.info("success,return body = " + body);
            } else if (statusCode == 204) {
                //处理成功，无返回Body
                log.info("success");
            } else {
                log.info("failed,resp code = " + statusCode + ",return body = " + body);
                throw new IOException("request failed");
            }

            HashMap resultMap = gson.fromJson(body, HashMap.class);
            //拿二维码
            String codeUrl = (String) resultMap.get("code_url");
            orderInfoService.saveCodeUrl(orderInfo.getOrderNo(), codeUrl);
            Map<String, Object> map1 = new HashMap<>();
            map1.put("codeUrl", codeUrl);
            map1.put("orderNo", orderInfo.getOrderNo());
            return map1;
        } finally {
            response.close();
        }
    }

    @Override
    public void processOrder(Map<String, Object> body) throws Exception {
        log.info("处理订单");
        //解密报文
        String plainTest = decryptFromResource(body);
        //将明文转换为map
        Gson gson = new Gson();
        HashMap map = gson.fromJson(plainTest, HashMap.class);
        String orderNo = (String) map.get("out_trade_no");
        //采用数据锁进行并发控制 避免数据混乱
        if (lock.tryLock()) {
            try {
                //处理重复的通知
                String orderInfoStatus = orderInfoService.getOrderInfoStatus(orderNo);
                if (!OrderStatus.NOTPAY.getType().equals(orderInfoStatus)) {
                    return;
                }
                //更新订单状态
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);
                //记录支付日志
                paymentInfoService.createPaymentInfo(plainTest);
            } finally {
                //要主动释放锁
                lock.unlock();
            }
        }
    }

    @Override
    public void refundProcessOrder(Map<String, Object> body) throws Exception {
        log.info("处理订单");
        //解密报文
        String plainTest = decryptFromResource(body);
        //将明文转换为map
        Gson gson = new Gson();
        HashMap map = gson.fromJson(plainTest, HashMap.class);
        String orderNo = (String) map.get("out_trade_no");
        //采用数据锁进行并发控制 避免数据混乱
        if (lock.tryLock()) {
            try {
                //处理重复的通知
                String orderInfoStatus = orderInfoService.getOrderInfoStatus(orderNo);
                if (!OrderStatus.REFUND_PROCESSING.getType().equals(orderInfoStatus)) {
                    return;
                }
                //更新订单状态
                orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_SUCCESS);
                //更新退款记录
                refundInfoService.updateRefund(plainTest);
            } finally {
                //要主动释放锁
                lock.unlock();
            }
        }
    }

    @Override
    public void cancelOrder(String orderNo) throws Exception {
        //调用微信的取消订单接口
        this.closeOrder(orderNo);
        //更新状态
        orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CANCEL);
    }

    @Override
    public void closeOrder(String orderNo) throws Exception {
        log.info("调用微信关闭订单接口===>{}", orderNo);
        //domain微信服务器地址
        String url = String.format(WxApiType.CLOSE_ORDER_BY_NO.getType(), orderNo);
        url = wxPayConfig.getDomain().concat(url);
        HttpPost post = new HttpPost(url);
        Gson gson = new Gson();
        Map<String, String> map = new HashMap<>();
        map.put("mchid", wxPayConfig.getMchId());
        String json = gson.toJson(map);
        log.info("请求参数===>{}", json);
        //请求参数设置到请求对象中
        StringEntity entity = new StringEntity(json, "utf-8");
        entity.setContentType("application/json");
        post.setEntity(entity);
        post.setHeader("Accept", "application/json");
        //完成签名并执行请求
        CloseableHttpResponse response = wxPayClient.execute(post);


        try {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                log.info("成功");
            } else if (statusCode == 204) {
                log.info("成功");
            } else {
                log.info("失败");
                throw new Exception("request failed");
            }
        } finally {
            response.close();
        }
    }

    /**
     * 对称解密
     *
     * @param body
     * @return 明文
     */
    private String decryptFromResource(Map<String, Object> body) throws GeneralSecurityException {
        log.info("密文解密");

        AesUtil aesUtil = new AesUtil(wxPayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
        //associateDate nonce随机串 密文
        Map<String, String> resourceMap = (Map) body.get("resource");
        String ciphertext = resourceMap.get("ciphertext");
        String nonce = resourceMap.get("nonce");
        String associatedData = resourceMap.get("associated_data");
        log.info("密文===>{}", ciphertext);
        String plainTest = aesUtil.decryptToString(associatedData.getBytes(StandardCharsets.UTF_8), nonce.getBytes(StandardCharsets.UTF_8), ciphertext);
        log.info("明文===>{}", plainTest);
        return plainTest;
    }

    @Override
    public String queryOrder(String orderNo) throws Exception {
        log.info("查单接口调用===>{}", orderNo);
        String url = String.format(WxApiType.ORDER_QUERY_BY_NO.getType(), orderNo);
        url = wxPayConfig.getDomain().concat(url).concat("?mchid=").concat(wxPayConfig.getMchId());
        HttpGet get = new HttpGet(url);
        get.setHeader("Accept", "application/json");
        CloseableHttpResponse response = wxPayClient.execute(get);
        try {
            String body = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                //处理成功
                log.info("成功,return body = " + body);
            } else if (statusCode == 204) {
                //处理成功，无返回Body
                log.info("成功");
            } else {
                log.info("失败,resp code = " + statusCode + ",return body = " + body);
                throw new IOException("request failed");
            }
            return body;
        } finally {
            response.close();
        }
    }

    /**
     * 如果订单已支付，则更新商户端订单状态
     * 如果未支付,关闭订单
     *
     * @param orderNo
     */
    @Override
    public void checkOrderStatus(String orderNo) throws Exception {
        log.warn("根据订单号核实订单状态===>{}", orderNo);
        String result = queryOrder(orderNo);
        Gson gson = new Gson();
        HashMap map = gson.fromJson(result, HashMap.class);
        //获取订单状态
        Object status = map.get("trade_state_desc");
        if (OrderStatus.SUCCESS.getType().equals(status)) {
            log.warn("核实订单已支付===>{}", orderNo);
            //更新本地订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.SUCCESS);
            //记录支付日志
            paymentInfoService.createPaymentInfo(result);
        }

        if (OrderStatus.NOTPAY.getType().equals(status)) {
            log.warn("核实订单未支付===>{}", orderNo);
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.CLOSED);
        }
    }

    @Override
    public void refund(String orderNo, String reason) throws IOException {
        log.info("创建退款记录");
        RefundInfo refund = refundInfoService.creatRefundByOrderNo(orderNo, reason);
        log.info("调用退款api");
        //请求
        String url = wxPayConfig.getDomain().concat(WxApiType.DOMESTIC_REFUNDS.getType());
        HttpPost post = new HttpPost(url);
        //设置参数
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<>();
        map.put("out_trade_no", orderNo);
        map.put("out_refund_no", refund.getRefundNo());
        map.put("reason", refund.getReason());
        map.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxNotifyType.REFUND_NOTIFY.getType()));
        Map<String, Object> amount = new HashMap<>();
        amount.put("refund", refund.getRefund());
        amount.put("total", refund.getTotalFee());
        amount.put("currency", "CNY");
        map.put("amount", amount);
        String json = gson.toJson(map);

        log.info("请求参数===>{}", json);
        StringEntity entity = new StringEntity(json, "utf-8");
        entity.setContentType("application/json");
        post.setEntity(entity);
        post.setHeader("Accept", "application/json");

        CloseableHttpResponse response = wxPayClient.execute(post);
        try {
            String body = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                //处理成功
                log.info("成功,退款结果 = " + body);
            } else if (statusCode == 204) {
                //处理成功，无返回Body
                log.info("成功");
            } else {
                log.info("退款失败 = " + statusCode + ",退款结果 = " + body);
                throw new RuntimeException("退款失败");
            }

            //更新订单状态
            orderInfoService.updateStatusByOrderNo(orderNo, OrderStatus.REFUND_PROCESSING);
            //更新退款订单
            refundInfoService.updateRefund(body);
        } finally {
            response.close();
        }
    }

    @Override
    public String queryRefundByOrderNo(String orderNo) throws Exception {
        log.info("查询退款接口调用");
        //请求
        String url = String.format(WxApiType.DOMESTIC_REFUNDS_QUERY.getType(), orderNo);
        url = wxPayConfig.getDomain().concat(url);
        HttpGet get = new HttpGet(url);
        get.setHeader("Accept", "application/json");

        CloseableHttpResponse response = wxPayClient.execute(get);
        try {
            String body = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                //处理成功
                log.info("成功,退款查询结果 = " + body);
            } else if (statusCode == 204) {
                //处理成功，无返回Body
                log.info("成功");
            } else {
                log.info("查询异常 = " + statusCode + ",退款结果 = " + body);
                throw new RuntimeException("退款查询异常");
            }
            return body;
        } finally {
            response.close();
        }
    }

    /**
     * 申请账单
     *
     * @param billDate
     * @param type
     * @return
     * @throws Exception
     */
    @Override
    public String queryBill(String billDate, String type) throws Exception {
//        billDate=billDate.concat(" 0:00:00");
        log.warn("申请账单接口调用 {}", billDate);
        String url = "";
        if ("tradebill".equals(type)) {
            url = WxApiType.TRADE_BILLS.getType();
        } else if ("fundflowbill".equals(type)) {
            url = WxApiType.FUND_FLOW_BILLS.getType();
        } else {
            throw new RuntimeException("不支持的账单类型");
        }
        url = wxPayConfig.getDomain().concat(url).concat("?bill_date=").concat(billDate);
        //创建远程Get 请求对象
        HttpGet get = new HttpGet(url);
        get.addHeader("Accept", "application/json");
        //使用wxPayClient发送请求得到响应
        CloseableHttpResponse response = wxPayClient.execute(get);
        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                log.info("成功, 申请账单返回结果 = " + bodyAsString);
            } else if (statusCode == 204) {
                log.info("成功");
            } else {
                throw new RuntimeException("申请账单异常, 响应码 = " + statusCode + ", 申请账单返回结果 = " + bodyAsString);
            }
            //获取账单下载地址
            Gson gson = new Gson();
            Map<String, String> resultMap = gson.fromJson(bodyAsString, HashMap.class);
            return resultMap.get("download_url");
        } finally {
            response.close();
        }
    }

    /**
     * 下载账单
     *
     * @param billDate
     * @param type
     * @return
     * @throws Exception
     */
    @Override
    public String downloadBill(String billDate, String type) throws Exception {
        log.warn("下载账单接口调用 {}, {}", billDate, type);
        //获取账单url地址
        String downloadUrl = this.queryBill(billDate, type);
        //创建远程Get 请求对象
        HttpGet get = new HttpGet(downloadUrl);
        get.addHeader("Accept", "application/json");
        //使用wxPayClient发送请求得到响应
        CloseableHttpResponse response = wxPayNoSignClient.execute(get);
        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                log.info("成功, 下载账单返回结果 =" );
            } else if (statusCode == 204) {
                log.info("成功");
            } else {
                throw new RuntimeException("下载账单异常, 响应码 = " + statusCode + ", 下载账单返回结果 = " + bodyAsString);
            }
            return bodyAsString;
        } finally {
            response.close();
        }
    }
}
