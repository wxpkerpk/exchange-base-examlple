package com.bitcola.exchange.caculate.kafka;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.exchange.caculate.message.PushMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaSender {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send(PushMessage pushMessage) {
        kafkaTemplate.send("push-topic-wss", JSONObject.toJSONString(pushMessage));
    }

}
