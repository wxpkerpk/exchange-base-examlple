package com.bitcola.exchange.security.admin;

import com.ace.cache.EnableAceCache;
import com.bitcola.exchange.security.auth.client.EnableAceAuthClient;
import com.spring4all.swagger.EnableSwagger2Doc;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * ${DESCRIPTION}
 *
 * @author wx
 * @create 2017-05-25 12:44
 */
@EnableEurekaClient
@EnableCircuitBreaker
@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@EnableFeignClients({"com.bitcola.exchange.security.auth.client.feign","com.bitcola.exchange.security.admin.feign"})
@EnableScheduling
@EnableAceAuthClient
@EnableAceCache
@EnableTransactionManagement
@MapperScan("com.bitcola.exchange.security.admin.mapper")
@EnableSwagger2Doc
public class AdminBootstrap {
    public static void main(String[] args) {
        SpringApplication.run(AdminBootstrap.class, args);
    }
}
