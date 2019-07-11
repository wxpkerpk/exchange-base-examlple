package com.bitcola.exchange.security.community;


import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.bitcola.exchange.security.auth.client.EnableAceAuthClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
//@MapperScan("com.bitcola.exchange.security.me.mapper")
@EnableEurekaClient
@EnableCircuitBreaker
@EnableFeignClients({"com.bitcola.exchange.security.auth.client.feign","com.bitcola.exchange.security.community.feign"})
@EnableScheduling
@EnableAceAuthClient
@EnableMethodCache(basePackages = "com.bitcola")
@EnableDiscoveryClient
@EnableTransactionManagement
public class CommunityApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}
}
