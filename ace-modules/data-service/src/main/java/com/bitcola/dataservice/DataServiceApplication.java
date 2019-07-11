package com.bitcola.dataservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.bitcola.dataservice.mapper")
@EnableEurekaClient
@EnableFeignClients
@EnableCircuitBreaker
//@EnableFeignClients({"com.bitcola.exchange.security.auth.client.feign"})
//@EnableScheduling
//@EnableAceAuthClient
@EnableTransactionManagement
public class DataServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataServiceApplication.class, args);



	}
}
