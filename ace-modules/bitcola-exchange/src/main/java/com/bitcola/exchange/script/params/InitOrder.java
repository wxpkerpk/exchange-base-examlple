package com.bitcola.exchange.script.params;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-05-15 18:59
 **/
@Data
public class InitOrder {
    String pair;
    BigDecimal currentPrice;

    BigDecimal minRate;
    BigDecimal maxRate;

    BigDecimal minNumber;
    BigDecimal maxNumber;
    Integer size;
}
