package com.bitcola.exchange.caculate.kafka;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bitcola.exchange.caculate.config.WebSocket;
import com.bitcola.exchange.caculate.config.WebSocketConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class KafkaConsumer {
    @Autowired
    WebSocket webSocket;


    @KafkaListener(topics = {"push-topic-wss"})
    public void consumer1(ConsumerRecord<?, ?> consumerRecord) {
        String value=consumerRecord.value().toString();
        var json= JSONObject.parseObject(value);
        webSocket.sendMessageToTopic(json.getString("topic"),json);
    }

}

