package com.bitcola.exchange.security.common.msg;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-03-07 11:45
 **/
@Data
public class ColaChainOrder {
    String orderId;
    String coinCode;
    String address;
    String memo;
    BigDecimal number;
}
