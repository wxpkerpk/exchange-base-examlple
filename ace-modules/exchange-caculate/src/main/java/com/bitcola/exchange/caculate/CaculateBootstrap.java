package com.bitcola.exchange.caculate;


import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.bitcola.exchange.klock.KlockAutoConfiguration;
import com.bitcola.exchange.security.auth.client.EnableAceAuthClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.math.BigDecimal;

/*
 * @author:wx
 * @description: 服务启动
 * @create:2018-07-22  01:35
 */
@EnableEurekaClient
@EnableCircuitBreaker
@SpringBootApplication(exclude = {MongoAutoConfiguration.class,MongoDataAutoConfiguration.class})
@EnableScheduling
@EnableFeignClients({"com.bitcola.exchange.security.auth.client.feign","com.bitcola.exchange.caculate"})
@EnableAceAuthClient
@EnableMethodCache(basePackages = "com.bitcola")
@EnableCreateCacheAnnotation
@Import({KlockAutoConfiguration.class})
public class CaculateBootstrap {


    public static void main(String[] args) {


            new SpringApplicationBuilder(CaculateBootstrap.class).run(args);
    }


}
