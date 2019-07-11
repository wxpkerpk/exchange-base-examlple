package com.bitcola.chain.chain.eos.entity;

import lombok.Data;

/**
 * @author zkq
 * @create 2018-12-08 17:22
 **/
@Data
public class EosTransaction {
    String trx_id;
    String code;
    String memo;
    String quantity;
    String receiver;
    String sender;
    String status;
    String symbol;
    String timestamp;
}
