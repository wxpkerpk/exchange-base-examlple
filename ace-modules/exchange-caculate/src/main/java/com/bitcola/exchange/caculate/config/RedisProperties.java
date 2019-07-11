package com.bitcola.exchange.caculate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/*
 * @author:wx
 * @description:
 * @create:2018-10-08  23:26
 */
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedisProperties {

    String host;

    int port;
    int timeout;

    String password;
}
