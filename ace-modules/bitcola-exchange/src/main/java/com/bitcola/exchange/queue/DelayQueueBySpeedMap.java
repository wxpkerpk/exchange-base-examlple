package com.bitcola.exchange.queue;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DelayQueueBySpeedMap<T> {

    int maxDelay;
    int minDelay;
    float speedPerCount;

    public DelayQueueBySpeedMap(int maxDelay,int minDelay,float speedPerCount){
        this.maxDelay = maxDelay;
        this.minDelay = minDelay;
        this.speedPerCount = speedPerCount;
    };

    Map<String, DelayQueueBySpeed<T>> map = new ConcurrentHashMap<>();

    public List<T> getMessage(String pair){
        return map.computeIfAbsent(pair,k -> new DelayQueueBySpeed<T>(maxDelay,minDelay,speedPerCount)).getMessage();
    }

    public void putMessage(String pair,T t){
        DelayQueueBySpeed<T> queue = map.computeIfAbsent(pair,k -> new DelayQueueBySpeed<T>(maxDelay,minDelay,speedPerCount));
        queue.putMessage(t);
    }


}

