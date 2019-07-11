package com.bitcola.chain.chain.tera.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-03-08 11:59
 **/
@Data
public class Send {
    long FromID;
    String FromPrivKey;
    long ToID;
    BigDecimal Amount;
    String Description = "BitCola Withdraw";
    int Wait = 1;
    public Send(long fromID,String fromPrivKey,long toID,BigDecimal amount){
        this.FromID = fromID;
        this.FromPrivKey = fromPrivKey;
        this.ToID = toID;
        this.Amount = amount;
    }
}
