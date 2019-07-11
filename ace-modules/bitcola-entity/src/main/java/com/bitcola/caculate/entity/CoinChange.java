package com.bitcola.caculate.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/*
 * @author:wx
 * @description:改变余额
 * @create:2018-07-30  22:55
 */
@Data
public class CoinChange implements Serializable {
    String userID;
    String coinCode;
    BigDecimal gain;
    String type;
    BigDecimal from;
    BigDecimal to;
    String orderId;

}
