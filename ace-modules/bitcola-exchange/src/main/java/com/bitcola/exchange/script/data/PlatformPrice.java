package com.bitcola.exchange.script.data;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-03-19 19:17
 **/
@Data
public class PlatformPrice {
    String pair;
    BigDecimal price;
    public PlatformPrice(String pair, BigDecimal price){
        this.pair = pair;
        this.price = price;
    }
}
