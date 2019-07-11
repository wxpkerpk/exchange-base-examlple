package com.bitcola.exchange.ctc;

import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.bitcola.exchange.security.auth.client.EnableAceAuthClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.bitcola.exchange.ctc.mapper")
@EnableEurekaClient
@EnableCircuitBreaker
@EnableFeignClients({"com.bitcola.exchange.security.auth.client.feign","com.bitcola.exchange.ctc.feign"})
@EnableScheduling
@EnableAceAuthClient
@EnableMethodCache(basePackages = "com.bitcola.exchange.ctc")
@EnableTransactionManagement
@EnableDiscoveryClient
public class BitColaExchangeCtcApplication {
	public static void main(String[] args) {
		SpringApplication.run(BitColaExchangeCtcApplication.class, args);
	}
}
