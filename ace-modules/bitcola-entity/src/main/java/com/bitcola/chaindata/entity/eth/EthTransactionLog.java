package com.bitcola.chaindata.entity.eth;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/*
 * @author:wx
 * @description:
 * @create:2018-11-11  22:24
 */
@Data
public class EthTransactionLog implements Serializable {

    long blockNumber;
    long timeStamp;
    String hash;
    String from;
    String to;
    BigDecimal value;
    long gas;
    long gasPrice;
    int isError;
    long gasUsed;
    long confirmations;

}
