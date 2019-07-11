//package com.bitcola.exchange.security.me.config;
//
//import com.bitcola.exchange.security.common.constant.ChainConstant;
//import com.bitcola.exchange.security.me.consumer.RedisReceive;
//import org.springframework.boot.autoconfigure.AutoConfigureAfter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.listener.PatternTopic;
//import org.springframework.data.redis.listener.RedisMessageListenerContainer;
//import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
//
//
///**
// * @author zkq
// * @create 2018-10-21 18:23
// **/
//@Configuration
//@AutoConfigureAfter({RedisReceive.class})
//public class SubscriberConfig {
//
//    /**
//     * 消息监听适配器，注入接受消息方法，输入方法名字 反射方法
//     *
//     * @param receiver
//     * @return
//     */
//    @Bean
//    public MessageListenerAdapter getMessageListenerAdapter(RedisReceive receiver) {
//        return new MessageListenerAdapter(receiver, "handleResponse"); //当没有继承MessageListener时需要写方法名字
//    }
//
//    /**
//     * 创建消息监听容器
//     *
//     * @param redisConnectionFactory
//     * @param messageListenerAdapter
//     * @return
//     */
//    @Bean
//    public RedisMessageListenerContainer getRedisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory, MessageListenerAdapter messageListenerAdapter) {
//        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
//        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
//        redisMessageListenerContainer.addMessageListener(messageListenerAdapter, new PatternTopic(ChainConstant.RESPONSE_TOPIC));
//        return redisMessageListenerContainer;
//    }
//}
//
