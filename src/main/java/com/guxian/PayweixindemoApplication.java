package com.guxian;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.guxian.mapper")
//定时任务
@EnableScheduling
public class PayweixindemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayweixindemoApplication.class, args);
    }

}
