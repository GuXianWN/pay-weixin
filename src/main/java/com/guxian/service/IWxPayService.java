package com.guxian.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * @author GuXianWN
 * 微信支付接口
 */
public interface IWxPayService {

    Map<String, Object> nativePay(Long productId) throws Exception;

    void processOrder(Map<String, Object> body) throws Exception;

    void refundProcessOrder(Map<String, Object> body) throws Exception;

    void cancelOrder(String orderNo) throws Exception;

    void closeOrder(String orderNo) throws Exception;

    String queryOrder(String orderNo) throws Exception;

    void checkOrderStatus(String orderNo) throws Exception;

    void refund(String orderNo, String reason) throws Exception;

    String queryRefundByOrderNo(String orderNo) throws Exception;

    String queryBill(String billDate, String type) throws Exception;

    String downloadBill(String billDate, String type) throws Exception;
}
