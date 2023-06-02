package com.guxian.controller;

import com.google.gson.Gson;
import com.guxian.entity.RespBean;
import com.guxian.service.impl.WxPayServiceImpl;
import com.guxian.util.HttpUtils;
import com.guxian.util.WechatPay2ValidatorForRequest;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付
 *
 * @author GuXianWN
 * @date 2021/12/19 14:12
 **/
@RequestMapping("/api/wx-pay")
@RestController
@CrossOrigin
@Api(tags = "微信支付API")
@Slf4j
public class WxPayController {
    @Resource
    private WxPayServiceImpl wxPayService;
    @Resource
    private Verifier verifier;


    @ApiOperation("调用统一下单api,生成支付二维码")
    @PostMapping("native/{productId}")
    public RespBean nativePay(@PathVariable Long productId) throws Exception {
        log.info("发起支付请求");
        //返回支付二维码和订单号
        Map<String, Object> map = wxPayService.nativePay(productId);
        return RespBean.success().setData(map);
    }

    @ApiOperation("通知")
    @PostMapping("/native/notify")
    public String nativeNotify(HttpServletRequest request, HttpServletResponse response) {
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<>();
        try {
            //处理通知参数
            String data = HttpUtils.readData(request);
            Map<String, Object> body = gson.fromJson(data, HashMap.class);
            String requestId = (String) body.get("id");
            log.info("支付通知id====>{}", requestId);
            log.info("通知的完整数据{}", data);
            //签名验证
            WechatPay2ValidatorForRequest wechatPay2ValidatorForRequest = new WechatPay2ValidatorForRequest(verifier, data, requestId);
            if (!wechatPay2ValidatorForRequest.validate(request)) {
                log.info("通知验签失败");
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("message", "通知验签失败");
                return gson.toJson(map);
            }
            log.info("验签成功");
            //处理订单
            wxPayService.processOrder(body);

            //通知应答

            //成功应答
            response.setStatus(200);
            map.put("code", "SUCCESS");
            map.put("message", "成功");
            return gson.toJson(map);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            map.put("code", "ERROR");
            map.put("message", "失败");
            return gson.toJson(map);
        }
    }

    @ApiOperation("取消订单")
    @PostMapping("cancel/{orderNo}")
    public RespBean cancel(@PathVariable String orderNo) throws Exception {
        log.info("取消订单");
        wxPayService.cancelOrder(orderNo);
        return RespBean.success().setMessage("订单已取消");
    }

    @ApiOperation("查询订单")
    @GetMapping("query/{orderNo}")
    public RespBean queryOrder(@PathVariable String orderNo) throws Exception {
        log.info("查询订单");
        String result = wxPayService.queryOrder(orderNo);
        return RespBean.success().setMessage("查询成功").data("result", result);
    }

    @ApiOperation("退款")
    @PostMapping("refunds/{orderNo}/{reason}")
    public RespBean refunds(@PathVariable String orderNo, @PathVariable String reason) throws IOException {
        log.info("申请退款===>{}", orderNo);
        wxPayService.refund(orderNo, reason);
        return RespBean.success();
    }

    @ApiOperation("查询退款")
    @PostMapping("query-refund/{orderNo}")
    public RespBean queryRefund(@PathVariable String orderNo) throws Exception {
        log.info("查询退款===>{}", orderNo);
        String result = wxPayService.queryRefundByOrderNo(orderNo);
        return RespBean.success().data("result", result);
    }

    /**
     * 微信把退款结果发到本接口
     *
     * @param request
     * @param response
     * @return
     */
    @ApiOperation("退款通知")
    @PostMapping("refunds/notify")
    public String refundsNotify(HttpServletRequest request, HttpServletResponse response) {
        log.info("退款通知执行");
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<>();

        try {
            //处理通知参数
            String data = HttpUtils.readData(request);
            Map<String, Object> body = gson.fromJson(data, HashMap.class);
            String requestId = (String) body.get("id");
            log.info("支付通知id====>{}", requestId);
            log.info("通知的完整数据{}", data);
            //签名验证
            WechatPay2ValidatorForRequest wechatPay2ValidatorForRequest = new WechatPay2ValidatorForRequest(verifier, data, requestId);
            if (!wechatPay2ValidatorForRequest.validate(request)) {
                log.info("通知验签失败");
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("message", "通知验签失败");
                return gson.toJson(map);
            }
            log.info("验签成功");
            //处理订单
            wxPayService.refundProcessOrder(body);

            //通知应答

            //成功应答
            response.setStatus(200);
            map.put("code", "SUCCESS");
            map.put("message", "成功");
            return gson.toJson(map);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            map.put("code", "ERROR");
            map.put("message", "失败");
            return gson.toJson(map);
        }
    }

    @ApiOperation("下载账单")
    @GetMapping("/downloadbill/{billDate}/{type}")
    public RespBean downloadBill(@PathVariable String billDate, @PathVariable String type) throws Exception {
        log.info("下载账单");
        String result = wxPayService.downloadBill(billDate, type);
        return RespBean.success().data("result", result);
    }
}
