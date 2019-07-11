package com.bitcola.exchange.script.data;

import lombok.Data;

/**
 * @author zkq
 * @create 2019-03-18 18:49
 **/
@Data
public class PairScale {
    String pair;
    Integer priceScale;
    Integer amountScale;
}
