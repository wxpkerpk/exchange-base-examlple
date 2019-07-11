package com.bitcola.chain.kafka.cosumer;


import com.alibaba.fastjson.JSONObject;
import com.bitcola.chain.cache.ChainCache;
import com.bitcola.chain.constant.Client;
import com.bitcola.chain.constant.MessageType;
import com.bitcola.chain.entity.BitColaChainMessage;
import com.bitcola.chain.handle.MessageRequestHandler;
import com.bitcola.chain.kafka.producer.PublisherAdapter;
import com.bitcola.chain.proxy.MessageRequestProxy;
import com.bitcola.exchange.security.common.util.MD5Utils;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Log4j2
@Service
public class Receiver {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    PublisherAdapter publisherAdapter;

    @Value("${bitcola.chain.kafka-token}")
    public String token;

    @Autowired
    MessageRequestHandler messageRequestHandler;

    ExecutorService cachedThreadPool = Executors.newFixedThreadPool(40);

    @KafkaListener(topics = {"${topicName}"},groupId = "${groupId}")
    public void onMessage(ConsumerRecord<?, ?> consumerRecord) {
        String string = consumerRecord.value().toString();
        if (log.isDebugEnabled()){
            log.debug("收到消息:"+string);
        }
        BitColaChainMessage message = JSONObject.parseObject(string, BitColaChainMessage.class);
        if (!checkSign(message)) return;
        // 判断消息类型,选择处理方式
        if (message.getMType() == MessageType.MODULE){
            handleModuleMessage(message);
        } else if (message.getMType() == MessageType.REPLAY){
            handleReplayMessage(message);
        } else if (message.getMType() == MessageType.NORMAL){
            handleNormalMessage(message);
        }
    }



    private void handleNormalMessage(BitColaChainMessage message){
        cachedThreadPool.submit(()->{
            try {
                final String key="chain-module-request:"+message.getId();
                Boolean requestTag= redisTemplate.opsForValue().setIfAbsent(key,"1",30, TimeUnit.SECONDS);
                if(requestTag) {
                    handleMessage(message);
                }
            } catch (Throwable e) {
                dealError(e,message);
            }
        });
    }

    private void handleReplayMessage(BitColaChainMessage message){
        String toClientId = message.getToClientId();
        if (Client.CLIENT_ID.equals(toClientId)){
            Object lock = MessageRequestProxy.locks.get(message.getId());
            if (lock!=null){
                MessageRequestProxy.messages.put(message.getId(),message);
                synchronized (lock){
                    lock.notifyAll();
                }
                // 删除锁
                MessageRequestProxy.locks.remove(message.getId());
            }
        }
    }
    private void handleModuleMessage(BitColaChainMessage message){
        String module = message.getToModule().toUpperCase();
        if (ChainCache.modules.contains(module)){
            cachedThreadPool.submit(()->{
                try {
                    handleMessage(message);
                } catch (Throwable e) {
                    dealError(e,message);
                }
            });
        }
    }


    private void handleMessage(BitColaChainMessage message) throws Throwable{
        String path = message.getPath();
        Object result = messageRequestHandler.handleRequest(path, message.getParams());
        publisherAdapter.sendReplay(result,message.getFromClientId(),message.getId());
    }


    private boolean checkSign(BitColaChainMessage message){
        // 判断签名是否正确
        String key = message.getId()+String.valueOf(message.getTime())+"BitColaKafkaPrivateKey";
        if (!MD5Utils.MD5(key).equals(message.getSign())){
            return false;
        }
        if (Math.abs(message.getTime()-System.currentTimeMillis())>10*60*60*1000){
            return false;
        }
        return true;
    }

    private void dealError(Throwable e,BitColaChainMessage message){
        BitColaChainMessage errorMessage = BitColaChainMessage.getINSTANCE(token,message.getId()).getErrorMessage(message.getFromClientId(), e.getMessage());
        publisherAdapter.send(errorMessage);
        log.error(e.getMessage(),e);
    }

}