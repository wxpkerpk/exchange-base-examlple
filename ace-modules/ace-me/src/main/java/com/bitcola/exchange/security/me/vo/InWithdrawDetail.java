package com.bitcola.exchange.security.me.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2018-10-23 17:25
 **/
@Data
public class InWithdrawDetail {
    private String orderId;
    private String coinCode;
    private String icon;
    private BigDecimal number;
    private BigDecimal worth;
    private String type;
    private String txId;
    private String address;
    private Long time;
    private BigDecimal toAccount;
    private BigDecimal fees;
    private String status;
    private String confirmations;
    private String blockBrowser;
    private String note;
}
