package com.bitcola.exchange.data;

import lombok.Data;

import java.io.Serializable;

/*
 * @author:wx
 * @description:
 * @create:2018-11-02  23:21
 */
@Data
public class TransactionSignPair implements Serializable {
    String userId;
    String token;
}
