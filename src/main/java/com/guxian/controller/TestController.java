package com.guxian.controller;

import com.guxian.config.WxPayConfig;
import com.guxian.entity.RespBean;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author GuXianWN
 * @date 2021/12/18 21:51
 * 测试控制器
 **/
@Api(tags = "测试")
@RestController
@RequestMapping("/api/test")
public class TestController {
    @Resource
    private WxPayConfig wxPayConfig;

    @GetMapping("getWxPayConfig")
    public RespBean getWxPayConfig(){
        String mcId=wxPayConfig.getMchId();
        return RespBean.success().data("mcId",mcId);
    }
}
