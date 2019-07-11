package com.bitcola.exchange.security.auth.client.configuration;

import com.bitcola.exchange.security.auth.client.config.ServiceAuthConfig;
import com.bitcola.exchange.security.auth.client.config.UserAuthConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by wx on 2017/9/15.
 */
@Configuration
@ComponentScan({"com.bitcola.exchange.security.auth.client","com.bitcola.exchange.security.auth.common.event"})
public class AutoConfiguration {
    @Bean
    ServiceAuthConfig getServiceAuthConfig(){
        return new ServiceAuthConfig();
    }

    @Bean
    UserAuthConfig getUserAuthConfig(){
        return new UserAuthConfig();
    }

}
