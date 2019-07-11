package com.bitcola.exchange.security.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * ${DESCRIPTION}
 *
 * @author wx
 * @create 2017-05-25 12:44
 */
@SpringBootApplication
//@EnableAdminServer
@EnableEurekaClient
public class MonitorBootstrap {
    public static void main(String[] args) {
        SpringApplication.run(MonitorBootstrap.class, args);
    }
}
