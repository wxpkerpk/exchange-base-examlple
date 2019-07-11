package com.bitcola.chain.chain.xem.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2018-11-23 19:22
 **/
@Data
public class Signature {
    long deadline;
    BigDecimal fee;
    String otherAccount;
    Hash otherHash;
    String signature;
    String signer;
    long timeStamp;
    int type;
    long version;

}
