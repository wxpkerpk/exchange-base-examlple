package com.bitcola.exchange.security.me.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 冲提记录
 *
 * @author zkq
 * @create 2018-10-23 16:11
 **/
@Data
public class InWithdrawListVo {

    String id;
    String txId;
    String coinCode;
    BigDecimal number;
    Long time;
    String status;
    String confirmation;
    String type;
    String icon;
    Integer scale;
}
