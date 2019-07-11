package com.bitcola.exchange.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-02-13 16:37
 **/
@Data
public class Depth {
    int index;
    BigDecimal price;
    BigDecimal number;
    public Depth(int index,BigDecimal price,BigDecimal number){
        this.index = index;
        this.price = price;
        this.number = number;
    }
}
