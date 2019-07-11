package com.bitcola.exchange.security.me.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 资金记录
 *
 * @author zkq
 * @create 2018-10-24 19:24
 **/
@Data
public class FinancialRecordsVo {
    String id;
    long time;
    String coinCode;
    String actionType;
    BigDecimal account;
    String status;
    Integer scale;
}
