package com.bitcola.exchange.security.common.msg;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-04-01 15:44
 **/
@Data
public class ColaChainBalance {

    String coinCode;
    BigDecimal balance;
    String feeCoinCode;
    BigDecimal feeBalance;
    /**
     * 手续费余额小于这个数量提币进入审核
     */
    BigDecimal feeLimit;
    String module;
}
