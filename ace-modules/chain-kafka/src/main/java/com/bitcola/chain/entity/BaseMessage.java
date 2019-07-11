package com.bitcola.chain.entity;

import com.bitcola.chain.constant.Client;
import com.bitcola.chain.constant.MessageType;
import lombok.Data;

/**
 * @author zkq
 * @create 2019-01-18 19:18
 **/
@Data
public abstract class BaseMessage extends Message{
    String id;
    long time;
    /**
     * 消息类型(0普通消息,1回复消息,2指定消息处理端的消息)
     */
    int mType = MessageType.NORMAL;
    /**
     * 消息是回复给哪个人,如果这个值是空,表示所有客户端抢占
     */
    String toClientId;
    /**
     * 自己的 id
     */
    String fromClientId = Client.CLIENT_ID;
    /**
     * 发送给执行的模块
     */
    String toModule;
    String sign;
    public abstract BaseMessage getNormalMessage();
    public abstract BaseMessage getModuleMessage(String module);
    public abstract BaseMessage getReplayMessage(String toClientId);
    public abstract BaseMessage getErrorMessage(String toClientId,String errorMsg);

}
