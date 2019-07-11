package com.bitcola.exchange.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-02-15 10:06
 **/
@Data
public class KlineDbData {
    String pair;
    BigDecimal price;
    BigDecimal number;
}
