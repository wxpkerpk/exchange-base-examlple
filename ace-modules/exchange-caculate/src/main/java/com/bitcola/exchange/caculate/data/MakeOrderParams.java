package com.bitcola.exchange.caculate.data;

import lombok.Data;

import java.math.BigDecimal;

/*
 * @author:wx
 * @description:
 * @create:2018-08-12  00:58
 */
@Data
public class MakeOrderParams {
    String code;
    BigDecimal price;
    BigDecimal count;
    String type;
    String sign;
    String signKey;
    long time;
    BigDecimal total;
}
