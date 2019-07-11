package com.bitcola.dataservice.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration //@Configuration 与 @Component 均可
public class ExcuteServiceConfig {
    @Bean(name = "rewardPoll")
    public ExecutorService getThreadPool() {
        return Executors.newFixedThreadPool(16);
    }

}
