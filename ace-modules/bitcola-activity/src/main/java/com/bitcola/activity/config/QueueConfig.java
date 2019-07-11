package com.bitcola.activity.config;

import com.bitcola.activity.msg.BuyMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author zkq
 * @create 2019-05-12 10:15
 **/
@Configuration
public class QueueConfig {

    @Bean("buyQueue")
    public BlockingQueue<BuyMessage> createBlockQueue(){
        return new LinkedBlockingQueue<>();
    }

}
