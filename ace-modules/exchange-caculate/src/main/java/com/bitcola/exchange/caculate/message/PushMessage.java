package com.bitcola.exchange.caculate.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class PushMessage implements Serializable {
    public PushMessage(String topic, Object data) {
        this.topic = topic;
        this.data = data;
    }

    String topic;
    Object data;
}
