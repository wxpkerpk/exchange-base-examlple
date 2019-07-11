package com.bitcola.exchange.security.me.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 资金项
 *
 * @author zkq
 * @create 2018-09-11 16:24
 **/
@Data
public class BalanceItemVo {
    private String coinCode;
    private String icon;
    private BigDecimal available;
    private BigDecimal frozen;
    private BigDecimal total;
    private BigDecimal worth;
    private BigDecimal change;
    private int allowDeposit = 1;
    private int allowWithdraw = 1;
    private int scale;
    private int isNeedNote = 0;
}
