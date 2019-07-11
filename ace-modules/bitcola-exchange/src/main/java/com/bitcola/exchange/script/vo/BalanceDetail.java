package com.bitcola.exchange.script.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-03-29 16:06
 **/
@Data
public class BalanceDetail {
    String coinCode;
    BigDecimal aAvailable = BigDecimal.ZERO;
    BigDecimal aFrozen = BigDecimal.ZERO;
    BigDecimal aTotal = BigDecimal.ZERO;
    BigDecimal aWorth = BigDecimal.ZERO;
    BigDecimal bAvailable = BigDecimal.ZERO;
    BigDecimal bFrozen = BigDecimal.ZERO;
    BigDecimal bTotal = BigDecimal.ZERO;
    BigDecimal bWorth = BigDecimal.ZERO;

}
