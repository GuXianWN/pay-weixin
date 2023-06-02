package com.guxian;

import com.guxian.config.WxPayConfig;
import com.guxian.service.impl.WxPayServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.security.PrivateKey;

@SpringBootTest
class PayweixindemoApplicationTests {
    @Resource
    private WxPayServiceImpl wxPayService;

//    @Test
//    public void queryTradeBillTest() throws Exception {
//        String billDate="2021-12-20";
//        String type="fundflowbill";
//        String downloadUrl = wxPayService.queryBill(billDate, type);
//    }
}
