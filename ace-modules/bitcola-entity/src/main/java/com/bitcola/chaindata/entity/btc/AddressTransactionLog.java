package com.bitcola.chaindata.entity.btc;

import lombok.Data;

import java.math.BigDecimal;

/*
 * @author:wx
 * @description:
 * @create:2018-10-21  15:27
 */

@Data
public class AddressTransactionLog {


    String txid;
    String address;
    BigDecimal amount;
    Integer confirmations;
    Boolean safe;



}
