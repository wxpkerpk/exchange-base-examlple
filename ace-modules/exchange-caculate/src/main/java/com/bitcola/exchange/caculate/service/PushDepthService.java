package com.bitcola.exchange.caculate.service;

import com.bitcola.exchange.caculate.config.WebSocket;
import com.bitcola.exchange.caculate.data.TransactionMessage;
import com.bitcola.exchange.caculate.kafka.KafkaSender;
import com.bitcola.exchange.caculate.message.PushMessage;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class PushDepthService extends PushCustomer {
    @Autowired
    KafkaSender kafkaSender;

    public List<String> klineTopics=new ArrayList<>();

    public static class DepthType {
        public String pair;
        public int scale;
        public int len;

        public String toTopic() {
            return pair + "_" + len + "_" + scale;
        }
    }
    @Override
    public void action(String pair,Object param) {
        //var coinTrader=MatchService.coinTraderMap.get(pair);
        var types=topic.get(pair);
        types.forEach(type->{
            if(type instanceof DepthType) {
               // var depth= coinTrader.getDepth(((DepthType) type).len,((DepthType) type).scale);
                //kafkaSender.send(new PushMessage(((DepthType) type).toTopic(),depth));
            }

        });
    }




}
