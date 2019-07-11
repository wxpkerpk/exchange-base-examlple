package com.bitcola.exchange.message;


import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @author zkq
 * @create 2019-02-12 12:42
 **/
@Data
public class NotifyMessage {
    public NotifyMessage(String topic, Object data) {
        this.topic = topic;
        this.data = data;
    }

    String topic;
    Object data;
    int c = 1; // currentPage
    int t = 1; // totalPage
    @Override
    public String toString(){
        return JSONObject.toJSONString(this);
    }
}
