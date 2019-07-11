package com.bitcola.exchange.security.me.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * ico 充值列表
 *
 * @author zkq
 * @create 2018-10-09 10:33
 **/
@Data
public class DepositVo {

    private long timestamp;

    private BigDecimal Amount;

    private String confirmationCount;

    private String depositStatus;
}
