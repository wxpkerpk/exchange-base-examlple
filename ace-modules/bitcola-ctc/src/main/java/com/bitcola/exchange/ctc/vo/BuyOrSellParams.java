package com.bitcola.exchange.ctc.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-05-09 11:40
 **/
@Data
public class BuyOrSellParams {
    BigDecimal number;
    String coinCode;
    String pin;
}
