package com.bitcola.chaindata.entity.btc;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-01-14 11:55
 **/
@Data
public class BtcTransaction {
    String account;
    String address;
    String category;
    BigDecimal amount;
    String label;
    int vout;
    int confirmations;
    String txid;
    Long time;
    Long timereceived;
}
