package com.bitcola.chain;


import com.ace.cache.EnableAceCache;
import com.alicp.jetcache.anno.EnableCache;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.bitcola.chain.mapper")
@EnableMethodCache(basePackages = "com.bitcola.chain")
public class ChainKafkaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChainKafkaServerApplication.class, args);
	}





}
