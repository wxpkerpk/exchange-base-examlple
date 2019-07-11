package com.bitcola.exchange.security.me;

import com.ace.cache.EnableAceCache;
import com.bitcola.exchange.security.auth.client.EnableAceAuthClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.bitcola.exchange.security.me.mapper")
@EnableEurekaClient
@EnableCircuitBreaker
@EnableFeignClients({"com.bitcola.exchange.security.auth.client.feign","com.bitcola.exchange.security.me.feign"})
@EnableScheduling
@EnableAceAuthClient
@EnableAceCache
@EnableTransactionManagement
@EnableDiscoveryClient
@ComponentScan(basePackages={"com.bitcola.exchange.security.me"})
public class MeApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeApplication.class, args);
	}
}
