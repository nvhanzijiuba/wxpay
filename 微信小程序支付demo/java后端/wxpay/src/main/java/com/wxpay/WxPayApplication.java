package com.wxpay;

import com.wxpay.config.IdWorker;
import com.wxpay.pojo.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WxPayApplication {
    public static void main(String[] args) {
        SpringApplication.run(WxPayApplication.class, args);
    }

    @Bean
    public IdWorker idWorker() {
        return new IdWorker();
    }
}
