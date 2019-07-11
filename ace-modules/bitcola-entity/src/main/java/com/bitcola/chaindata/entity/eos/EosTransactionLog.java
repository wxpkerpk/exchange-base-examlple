package com.bitcola.chaindata.entity.eos;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2018-12-13 15:29
 **/
@Data
public class EosTransactionLog {
    String sender;
    String receiver;
    BigDecimal number;
    String memo;
    String txId;
    String coin;
    /**
     * 合约名称
     */
    String coinName;
    boolean confirm;
}
