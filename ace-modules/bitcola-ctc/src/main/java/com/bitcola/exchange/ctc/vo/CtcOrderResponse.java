package com.bitcola.exchange.ctc.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-05-09 10:09
 **/
@Data
public class CtcOrderResponse {
    /**
     * 订单号
     */
    String id;
    String direction;
    String coinCode;
    Long timestamp;
    String customerUserId;
    BigDecimal price;
    BigDecimal number;
    String status;
}
