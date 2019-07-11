package com.bitcola.chain.chain.tt;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-04-09 12:00
 **/
@Data
public class TTTransaction {
    BigDecimal scale = new BigDecimal("1000000000000000000");
    long blockNumber;
    String from;
    String to;
    BigDecimal gasLimit;
    BigDecimal gasPrice;
    BigDecimal gasUsed;
    String hash;
    String inputData;
    BigDecimal nonce;
    boolean status;
    String timestamp;
    int transactionIndex;
    BigDecimal value;

    public BigDecimal getTTNumber(){
        if (value.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return value.divide(scale);
    }

}
