package com.bitcola.exchange.launchpad.config;


import com.bitcola.exchange.launchpad.message.BuyMessage;
import com.bitcola.exchange.launchpad.message.ClearMessage;
import com.bitcola.exchange.launchpad.vo.ResonanceBuyMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class MessageQueueConfig {


    @Bean
    DelayQueueBySpeedMap<ClearMessage> queueMap() {
        return new DelayQueueBySpeedMap<>(1000,200,50);
    }

    @Bean
    DelayQueueBySpeed<BuyMessage> buyQueue() {
        return new DelayQueueBySpeed<>(1000,200,50);
    }


    @Bean("resonanceBuyQueue")
    public BlockingQueue<ResonanceBuyMessage> createBlockQueue(){
        return new LinkedBlockingQueue<>();
    }


}
