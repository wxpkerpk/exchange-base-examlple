package com.bitcola.dataservice.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author zkq
 * @create 2018-10-17 20:31
 **/
@Component
public class SequenceFactory {
    @Autowired
    RedisTemplate<String, String> redisTemplate;

    /**
     * @param key
     * @param value
     * @param expireTime
     * @Title: set
     * @Description: set cache.
     */
    public void set(String key, int value, Date expireTime) {
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        counter.set(value);
        counter.expireAt(expireTime);
    }

    /**
     * @param key
     * @param value
     * @param timeout
     * @param unit
     * @Title: set
     * @Description: set cache.
     */
    public void set(String key, int value, long timeout, TimeUnit unit) {
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        counter.set(value);
        counter.expire(timeout, unit);
    }

    /**
     * @param key
     * @return
     * @Title: generate
     * @Description: Atomically increments by one the current value.
     */
    public long generate(String key) {
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        return counter.incrementAndGet();
    }

    /**
     * @param key
     * @return
     * @Title: generate
     * @Description: Atomically increments by one the current value.
     */
    public long generate(String key, long expireTime) {
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        counter.expire(expireTime,TimeUnit.SECONDS);
        return counter.incrementAndGet();
    }
    public long generate(String key, long expireTime,TimeUnit unit) {
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        counter.expire(expireTime,unit);
        return counter.incrementAndGet();
    }

    public void delete(String key){
        redisTemplate.delete(key);
    }

}