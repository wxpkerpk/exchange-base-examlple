package com.bitcola.exchange.data;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

/*
 * @author:wx
 * @description:
 * @create:2018-08-11  00:15
 */
@Data
public class Kline {
    BigDecimal open = BigDecimal.ZERO;
    BigDecimal high = BigDecimal.ZERO;
    BigDecimal low = BigDecimal.ZERO;
    BigDecimal close = BigDecimal.ZERO;
    BigDecimal vol = BigDecimal.ZERO;
    long time;
    public Number[] toArray(){
        return new Number[]{
                time,
                open.setScale(8, RoundingMode.HALF_UP).doubleValue(),
                high.setScale(8, RoundingMode.HALF_UP).doubleValue(),
                low.setScale(8, RoundingMode.HALF_UP).doubleValue(),
                close.setScale(8, RoundingMode.HALF_UP).doubleValue(),
                vol.setScale(8, RoundingMode.HALF_UP).doubleValue()
        };


    }

}
