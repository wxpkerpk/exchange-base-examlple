package com.bitcola.exchange.data;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/*
 * @author:wx
 * @description:首页数据
 * @create:2018-08-11  21:21
 */


@Data
public class MarketInfo implements Serializable {
    String pair;
    BigDecimal price;
    BigDecimal vol;
    BigDecimal gain_24;
    BigDecimal max_24h;
    BigDecimal min_24h;
    BigDecimal worth;

    String icon;
    Integer sort;
    Boolean isFav;
    long openTime;

}
