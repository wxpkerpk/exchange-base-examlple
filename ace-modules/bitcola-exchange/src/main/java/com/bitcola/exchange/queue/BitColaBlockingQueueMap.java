package com.bitcola.exchange.queue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zkq
 * @create 2019-02-22 18:34
 **/
public class BitColaBlockingQueueMap<T> {
    Map<String, BitColaBlockingQueue<T>> map = new ConcurrentHashMap<>();

    public List<T> getMessage(String pair){
        return map.computeIfAbsent(pair, k -> new BitColaBlockingQueue<>()).getMessage();
    }

    public void putMessage(String pair,T t){
        BitColaBlockingQueue<T> queue = map.computeIfAbsent(pair, k -> new BitColaBlockingQueue<>());
        queue.putMessage(t);
    }


}
