package com.bitcola.exchange.script.queue;

import com.bitcola.exchange.script.params.AutoMakeOrderParams;
import com.bitcola.exchange.script.params.DynamicDepthParams;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zkq
 * @create 2019-03-18 17:08
 **/
@Configuration
public class QueueConfig {

    @Bean("autoMakeOrderQueue")
    public ScriptDelayQueue<AutoMakeOrderParams> createAutoMakeOrderQueue(){
        return new ScriptDelayQueue<>();
    }

    @Bean("autoDynamicDepthQueue")
    public ScriptDelayQueue<DynamicDepthParams> createDynamicDepthQueue(){
        return new ScriptDelayQueue<>();
    }
}
