package com.bitcola.chain.entity;

import com.bitcola.chain.constant.MessageType;
import com.bitcola.exchange.security.common.util.MD5Utils;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * @author zkq
 * @create 2019-01-18 18:59
 **/
@Data
public class BitColaChainMessage extends BaseMessage {
    String path;
    Map<String,Object> params = new HashMap<>();


    public static BitColaChainMessage getINSTANCE(String key) {
        return getINSTANCE(key,UUID.randomUUID().toString());
    }

    public static BitColaChainMessage getINSTANCE(String key,String id) {
        BitColaChainMessage message = new BitColaChainMessage();
        long time = System.currentTimeMillis();
        message.setId(id);
        message.setTime(time);
        message.setSign(MD5Utils.MD5(message.getId()+String.valueOf(time)+key));
        return message;
    }

    @Override
    public BitColaChainMessage getNormalMessage() {
        return this;
    }

    @Override
    public BitColaChainMessage getReplayMessage(String toClientId) {
        this.setMType(MessageType.REPLAY);
        this.setToClientId(toClientId);
        return this;
    }

    @Override
    public BitColaChainMessage getErrorMessage(String toClientId,String errorMsg) {
        this.setMType(MessageType.REPLAY);
        this.setToClientId(toClientId);
        this.setSuccess(false);
        this.setErrorMsg(errorMsg);
        return this;
    }

    @Override
    public BitColaChainMessage getModuleMessage(String module) {
        this.setMType(MessageType.MODULE);
        this.setToModule(module);
        return this;
    }
}
