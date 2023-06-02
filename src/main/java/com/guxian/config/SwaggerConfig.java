package com.guxian.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author GuXianWN
 * @date 2021/12/17 14:21
 * swagger配置类
 **/
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo());
    }

    /**
     * @author GuXianWN
     * @date 2021/12/17 14:32
     * 配置文档信息
     **/
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("微信支付接口文档")
                .version("v1.0")
                .contact(new Contact("GuXianWN", "http://localhost:8090/doc.html", "2818958193@qq.com"))
                .build();
    }
}
