//package com.bitcola.exchange.security.me.config;
//
///*
// * @author:wx
// * @description:
// * @create:2018-10-21  17:30
// */
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.StringRedisTemplate;
//
///**
// * 注册redisTemplate，作为消息队列的发布者
// */
//@Configuration
//public class PublisherConfig {
//
//    @Bean
//    public StringRedisTemplate getRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        return new StringRedisTemplate(redisConnectionFactory);
//    }
//}