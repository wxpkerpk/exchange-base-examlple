package com.bitcola.exchange.security.me.dto;


import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户资金
 *
 * @author zkq
 * @create 2018-09-11 16:36
 **/
@Data
public class BalanceDto {

    private String coinCode;
    private String icon;
    private BigDecimal available;
    private BigDecimal frozen;
    private Integer prec;
    private Integer allowDeposit;
    private Integer allowWithdraw;
    private Integer isNeedNote;
}
