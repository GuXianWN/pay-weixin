package com.guxian.service.impl;

import com.google.gson.Gson;
import com.guxian.entity.PaymentInfo;
import com.guxian.enums.PayType;
import com.guxian.mapper.PaymentInfoMapper;
import com.guxian.service.IPaymentInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author GuXianWN
 * @since 2021-12-17
 */
@Service
@Slf4j
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements IPaymentInfoService {

    @Override
    public void createPaymentInfo(String plainTest) {
        log.info("记录支付日志");
        Gson gson = new Gson();
        HashMap map = gson.fromJson(plainTest, HashMap.class);
        PaymentInfo paymentInfo = new PaymentInfo();
        //订单号
        paymentInfo.setOrderNo((String) map.get("out_trade_no"));
        //支付方式
        paymentInfo.setPaymentType(PayType.WXPAY.getType());
        //交易id
        paymentInfo.setTransactionId((String) map.get("transaction_id"));
        //交易类型
        paymentInfo.setTradeType((String) map.get("trade_type"));
        //交易状态
        paymentInfo.setTradeState((String) map.get("trade_state"));
        //用户支付金额
        Map<String, Object> amount = (Map) map.get("amount");
        Integer payerTotal = ((Double) amount.get("payer_total")).intValue();
        paymentInfo.setPayerTotal(payerTotal);
        //把json也存进去
        paymentInfo.setContent(plainTest);
        baseMapper.insert(paymentInfo);
    }
}
