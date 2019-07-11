package com.bitcola.exchange.config;

import com.bitcola.exchange.message.KLineInsertMessage;
import com.bitcola.exchange.message.MatchMessage;
import com.bitcola.exchange.message.NotifyMessage;
import com.bitcola.exchange.message.OrderMessage;
import com.bitcola.exchange.queue.*;
import com.bitcola.exchange.websocket.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class MessageQueueConfig {
    /**
     * 下单队列
     * @return
     */
    @Bean("matchOrderQueue")
    LinkedBlockingQueueMap<OrderMessage> createMakeOrderQueue() {
        return new LinkedBlockingQueueMap<>();
    }

    /**
     * 清算队列
     * @return
     */
    @Bean("clearOrderQueue")
    BitColaBlockingQueueMap<MatchMessage> createClearOrderQueue() {
        return new BitColaBlockingQueueMap<>();
    }

    @Bean("kLineNotifyQueue")
    DelayQueueBySpeedMap<KlineNotifyMessage> createKLineNotifyQueue() {
        return new DelayQueueBySpeedMap<>(5000,1000,1000);
    }

    @Bean("depthNotifyQueue")
    DelayQueueBySpeedMap<DepthNotifyMessage> createDepthNotifyQueue() {
        return new DelayQueueBySpeedMap<>(1000,100,300);
    }
    @Bean("priceNotifyQueue")
    DelayQueueBySpeedMap<PriceNotifyMessage> createPriceNotifyQueue() {
        return new DelayQueueBySpeedMap<>(3000,100,300);
    }
    @Bean("klineInsertQueue")
    LinkedBlockingQueue<KLineInsertMessage> createKLineInsertQueue() {
        return new LinkedBlockingQueue<>();
    }
    @Bean("orderNotifyQueue")
    DelayQueueBySpeedMap<OrderNotifyMessage> createOrderNotifyQueue() {
        return new DelayQueueBySpeedMap<>(5000,100,300);
    }
    @Bean("personOrderNotifyQueue")
    DelayQueueBySpeed<PersonOrderNotifyMessage> createPersonOrderNotifyQueue() {
        return new DelayQueueBySpeed<>(1000,100,300);
    }





}
