package com.bitcola.chain.kafka.producer;

import com.bitcola.chain.entity.BitColaChainMessage;
import com.bitcola.exchange.security.common.util.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author zkq
 * @create 2019-01-18 17:29
 **/
@Service
public class PublisherAdapter {

    @Autowired
    Publisher publisher;

    @Value("${bitcola.chain.kafka-topic-send}")
    public String topic;

    @Value("${bitcola.chain.kafka-token}")
    public String token;

    /**
     * 普通发送
     * @param params
     */
    public void sendNormal(Map<String,Object> params, String path,String id) {
        BitColaChainMessage message = BitColaChainMessage.getINSTANCE(token,id).getNormalMessage();
        message.setParams(params);
        message.setPath(path);
        publisher.sendMessage(topic,message);
    }

    /**
     * 发送回复
     * @param result
     * @param toClientId
     */
    public void sendReplay(Object result,String toClientId,String id) {
        BitColaChainMessage message  = BitColaChainMessage.getINSTANCE(token,id).getReplayMessage(toClientId);
        message.setData(result);
        publisher.sendMessage(topic,message);
    }

    /**
     * 发送到指定模块执行
     * @param params
     * @param module
     */
    public void sendToModule(Map<String,Object> params,String module,String path,String id) {
        BitColaChainMessage message  = BitColaChainMessage.getINSTANCE(token,id).getModuleMessage(module);
        message.setParams(params);
        message.setPath(path);
        publisher.sendMessage(topic,message);
    }

    public void send(BitColaChainMessage message) {
        publisher.sendMessage(topic,message);
    }

}
