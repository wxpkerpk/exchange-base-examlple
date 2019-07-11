package com.bitcola.exchange.launchpad.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author zkq
 * @create 2019-02-23 18:03
 **/
public class LinkedBlockingQueueMap<T> {
    Map<String, LinkedBlockingQueue<T>> map = new ConcurrentHashMap<>();

    public T getMessage(String id){
        try {
            return map.computeIfAbsent(id, k -> new LinkedBlockingQueue<>()).take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void putMessage(String id,T t){
        LinkedBlockingQueue<T> queue = map.computeIfAbsent(id, k -> new LinkedBlockingQueue<>());
        try {
            queue.put(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
