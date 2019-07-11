package com.bitcola.chain.chain.xem.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zkq
 * @create 2018-11-23 18:47
 **/
@Data
public class Transaction {
    BigDecimal amount;
    Long deadline;
    BigDecimal fee;
    String recipient;
    String signature;
    String signer;
    Long timeStamp;
    // 257 是单签名 2049 远程账户 4097-4100 是多签名
    int type;

    // 多重签名需要
    List<Signature> signatures;
    Transaction otherTrans;


    long version;
    Message message;
    // 马赛克,代币在这个里面
    List<Mosaics> mosaics;
}
