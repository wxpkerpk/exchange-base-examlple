package com.bitcola.exchange.security.me.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 充值地址
 *
 * @author zkq
 * @create 2018-09-11 20:13
 **/
@Data
public class BalanceAddressVo {
    private String coinCode;
    private String icon;
    private String address;
    private int isNeedNote;
    private String note;
    private String description;
    private int confirm;
    private BigDecimal depositMin;
}
