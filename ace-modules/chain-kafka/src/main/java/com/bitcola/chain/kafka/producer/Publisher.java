package com.bitcola.chain.kafka.producer;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.chain.entity.BaseMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * @author zkq
 * @create 2019-01-18 17:25
 **/
@Service
@Log4j2
public class Publisher {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    public void sendMessage(String topic, BaseMessage value) {
        String json = JSONObject.toJSONString(value);
        if (log.isDebugEnabled()){
            log.debug("topic:"+topic+", message:"+ json);
        }
        try {
            kafkaTemplate.send(topic, json);
        } catch (Exception e) {
            log.error("消息发送失败了");
            log.error(e.getMessage(),e);
        }
    }
}
