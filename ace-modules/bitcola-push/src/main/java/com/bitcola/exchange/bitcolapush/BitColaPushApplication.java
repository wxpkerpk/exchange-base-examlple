package com.bitcola.exchange.bitcolapush;


import com.bitcola.exchange.security.auth.client.EnableAceAuthClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@EnableFeignClients({"com.bitcola.exchange.security.auth.client.feign"})
@EnableAceAuthClient
@EnableScheduling
public class BitColaPushApplication {

	public static void main(String[] args) {
		SpringApplication.run(BitColaPushApplication.class, args);
	}
}
