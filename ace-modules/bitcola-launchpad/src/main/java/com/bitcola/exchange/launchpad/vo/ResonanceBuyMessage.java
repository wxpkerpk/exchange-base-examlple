package com.bitcola.exchange.launchpad.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-05-12 10:18
 **/
@Data
public class ResonanceBuyMessage {
    BigDecimal amount;
    String pin;
    String coinCode;
    String symbol;
    String userId;
}
