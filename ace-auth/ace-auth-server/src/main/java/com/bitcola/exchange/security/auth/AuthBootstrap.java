package com.bitcola.exchange.security.auth;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Created by wx on 2017/6/2.
 */
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients({"com.bitcola.exchange.security.auth.client.feign","com.bitcola.exchange.security.auth.feign"})
@MapperScan("com.bitcola.exchange.security.auth.mapper")
@EnableAutoConfiguration
public class AuthBootstrap {
    public static void main(String[] args) {
        SpringApplication.run(AuthBootstrap.class, args);
    }
}
