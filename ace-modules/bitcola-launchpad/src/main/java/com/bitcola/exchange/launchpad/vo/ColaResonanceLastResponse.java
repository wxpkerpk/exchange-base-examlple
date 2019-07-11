package com.bitcola.exchange.launchpad.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-05-12 12:52
 **/
@Data
public class ColaResonanceLastResponse {
    String userId;
    BigDecimal number;
    Long timestamp;
    String symbol;
}
