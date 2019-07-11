package com.bitcola.exchange.data;

import lombok.Data;

import java.math.BigDecimal;

/*
 * @author:wx
 * @description:
 * @create:2018-08-12  00:58
 */
@Data
public class MakeOrderParams {
    String pair;
    BigDecimal price;
    BigDecimal number;
    String type;
    String sign;
    long time;
}
