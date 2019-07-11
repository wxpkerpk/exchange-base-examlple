package com.bitcola.exchange.caculate.service;

import com.bitcola.exchange.caculate.config.WebSocket;
import com.bitcola.exchange.caculate.data.TransactionMessage;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class PushCustomer implements InitializingBean {
    ConcurrentHashMap<String, Boolean> concurrentHashMap = new ConcurrentHashMap<>();

    @Autowired
    WebSocket webSocket;
    ExecutorService singlePool = Executors.newSingleThreadExecutor();

    Map<String,List<Object>> topic=new HashMap<>(32);//所属话题
    public abstract void action(String pair,Object params);

    public  DelayQueue<TransactionMessage> queue = new DelayQueue<>();



    public  Object getActionParams(String pair){

        return null;

    }

    @Override
    public void afterPropertiesSet() {
        singlePool.submit(() -> {
            while (true) {
                try {
                    var message = queue.take();
                    String pair = message.getBody();
                    concurrentHashMap.remove(pair);
                    action(pair,getActionParams(pair));

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    public void pushMessage(String pair, long delay) {
        if (concurrentHashMap.get(pair) == null) {
            concurrentHashMap.put(pair, true);
            TransactionMessage transactionMessage = new TransactionMessage(pair.hashCode(), pair, delay);
            queue.offer(transactionMessage);
        }
    }

}
