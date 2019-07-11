package com.bitcola.exchange.queue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author zkq
 * @create 2019-02-23 18:03
 **/
public class LinkedBlockingQueueMap<T> {
    Map<String, LinkedBlockingQueue<T>> map = new ConcurrentHashMap<>();

    public T getMessage(String pair){
        try {
            return map.computeIfAbsent(pair, k -> new LinkedBlockingQueue<>()).take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void putMessage(String pair,T t){
        LinkedBlockingQueue<T> queue = map.computeIfAbsent(pair, k -> new LinkedBlockingQueue<>());
        try {
            queue.put(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
