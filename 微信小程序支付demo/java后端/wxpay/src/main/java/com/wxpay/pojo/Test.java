package com.wxpay.pojo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "server")
@Data
public class Test {
    private String port;
    private String address;
}
