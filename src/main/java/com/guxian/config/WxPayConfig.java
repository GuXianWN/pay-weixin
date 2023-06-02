package com.guxian.config;

import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.ScheduledUpdateCertificatesVerifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

/**
 * @author GuXianWN
 * @date 2021/12/18 21:49
 * 微信支付配置类
 **/
@Configuration
@PropertySource("classpath:wxpay.properties") //读取配置文件
@ConfigurationProperties(prefix = "wxpay") //读取wxpay节点
@Data
@Slf4j
public class WxPayConfig {

    // 商户号
    private String mchId;

    // 商户API证书序列号
    private String mchSerialNo;

    // 商户私钥文件
    private String privateKeyPath;

    // APIv3密钥
    private String apiV3Key;

    // APPID
    private String appid;

    // 微信服务器地址
    private String domain;

    // 接收结果通知地址
    private String notifyDomain;

    // APIv2密钥
    private String partnerKey;

    /**
     * @author GuXianWN
     * @date 2021/12/19 13:44
     * 获取商户私钥
     */
    private PrivateKey getPrivateKey(String filename) {
        try {
            return PemUtil.loadPrivateKey(new FileInputStream(filename));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("私钥文件不存在", e);
        }
    }

    /**
     * @author GuXianWN
     * @date 2021/12/19 13:37
     * 获取签名验证器
     */
    @Bean
    public ScheduledUpdateCertificatesVerifier getVerifier() {
        PrivateKey privateKey = getPrivateKey(privateKeyPath);
        // 使用定时更新的签名验证器，不需要传入证书
        ScheduledUpdateCertificatesVerifier verifier = new ScheduledUpdateCertificatesVerifier(
                new WechatPay2Credentials(mchId, new PrivateKeySigner(mchSerialNo, privateKey)),
                apiV3Key.getBytes(StandardCharsets.UTF_8));
        return verifier;
    }

    /**
     * @param verifier 验证器
     * @author GuXianWN
     * @date 2021/12/19 13:37
     * 获取http请求对象
     */
    @Bean(name = "wxPayClient")
    public CloseableHttpClient getWxPayClient(ScheduledUpdateCertificatesVerifier verifier) {
        PrivateKey privateKey = getPrivateKey(privateKeyPath);
        // 使用定时更新的签名验证器，不需要传入证书
        verifier = new ScheduledUpdateCertificatesVerifier(
                new WechatPay2Credentials(mchId, new PrivateKeySigner(mchSerialNo, privateKey)),
                apiV3Key.getBytes(StandardCharsets.UTF_8));

        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                .withMerchant(mchId, mchSerialNo, privateKey)
                .withValidator(new WechatPay2Validator(verifier));
// ... 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient

// 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签，并进行证书自动更新
        return builder.build();
    }

    /**
     * 获取HttpClient，无需进行应答签名验证，跳过验签的流程
     */
    @Bean(name = "wxPayNoSignClient")
    public CloseableHttpClient getWxPayNoSignClient(){

        //获取商户私钥
        PrivateKey privateKey = getPrivateKey(privateKeyPath);

        //用于构造HttpClient
        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                //设置商户信息
                .withMerchant(mchId, mchSerialNo, privateKey)
                //无需进行签名验证、通过withValidator((response) -> true)实现
                .withValidator((response) -> true);

        // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签，并进行证书自动更新
        CloseableHttpClient httpClient = builder.build();

        log.info("== getWxPayNoSignClient END ==");

        return httpClient;
    }
}
