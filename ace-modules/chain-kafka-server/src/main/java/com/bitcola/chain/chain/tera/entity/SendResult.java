package com.bitcola.chain.chain.tera.entity;

import lombok.Data;

/**
 * @author zkq
 * @create 2019-03-08 12:02
 **/
@Data
public class SendResult {
    int result = 0;
    String TxID;
    String text;
}
