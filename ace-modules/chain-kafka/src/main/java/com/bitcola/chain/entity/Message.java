package com.bitcola.chain.entity;

import lombok.Data;

/**
 * @author zkq
 * @create 2019-01-19 16:11
 **/
@Data
public class Message {
    boolean isSuccess = true;
    String errorMsg;
    Object data;
}
