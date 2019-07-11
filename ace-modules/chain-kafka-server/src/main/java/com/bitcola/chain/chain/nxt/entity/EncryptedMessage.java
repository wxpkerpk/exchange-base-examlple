package com.bitcola.chain.chain.nxt.entity;

import lombok.Data;

/**
 * @author zkq
 * @create 2019-03-25 11:56
 **/
@Data
public class EncryptedMessage {
    String data;
    String nonce;
}
