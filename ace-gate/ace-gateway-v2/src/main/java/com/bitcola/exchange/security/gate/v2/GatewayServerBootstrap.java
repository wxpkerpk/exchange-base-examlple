package com.bitcola.exchange.security.gate.v2;

import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.bitcola.exchange.security.auth.client.EnableAceAuthClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author wx
 * @create 2018/3/12.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAceAuthClient
@EnableMethodCache(basePackages = "com.bitcola.exchange.security.gate.v2.feign")
@EnableCreateCacheAnnotation
@EnableFeignClients({"com.bitcola.exchange.security.auth.client.feign","com.bitcola.exchange.security.gate.v2.feign"})
@EnableScheduling
public class GatewayServerBootstrap {
    public static void main(String[] args) {
        SpringApplication.run(GatewayServerBootstrap.class, args);
    }



}
