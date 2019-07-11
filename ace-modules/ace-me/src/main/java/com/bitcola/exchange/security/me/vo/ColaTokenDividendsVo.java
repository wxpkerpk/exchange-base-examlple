package com.bitcola.exchange.security.me.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2018-10-26 17:00
 **/
@Data
public class ColaTokenDividendsVo {
    String id;
    Long time;
    String coinCode;
    BigDecimal tokenAmount;
    BigDecimal dividends;
    String status;
}
