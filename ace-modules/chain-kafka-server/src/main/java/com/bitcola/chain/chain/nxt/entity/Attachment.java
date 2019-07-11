package com.bitcola.chain.chain.nxt.entity;

import lombok.Data;

/**
 * @author zkq
 * @create 2019-03-12 10:37
 **/
@Data
public class Attachment {
    String message;
    EncryptedMessage encryptedMessage;
}
