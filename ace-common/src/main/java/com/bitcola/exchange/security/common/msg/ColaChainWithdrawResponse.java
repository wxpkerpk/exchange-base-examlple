package com.bitcola.exchange.security.common.msg;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-01-21 18:26
 **/
@Data
public class ColaChainWithdrawResponse {
    BigDecimal fee;
    String hash;
    String feeCoinCode;
    boolean success;
    String errMessage;
    String orderId;
}
