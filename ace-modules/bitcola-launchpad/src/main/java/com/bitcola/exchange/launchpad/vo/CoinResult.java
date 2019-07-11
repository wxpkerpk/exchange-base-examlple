package com.bitcola.exchange.launchpad.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-03-16 10:37
 **/
@Data
public class CoinResult {
    String coinCode;
    BigDecimal number;
    public CoinResult(String coinCode,BigDecimal number){
        this.coinCode = coinCode;
        this.number = number;
    }
}
