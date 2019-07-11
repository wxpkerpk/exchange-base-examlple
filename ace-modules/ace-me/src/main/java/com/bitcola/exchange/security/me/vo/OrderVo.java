package com.bitcola.exchange.security.me.vo;

import lombok.Data;

/**
 * 订单
 * @author zkq
 * @create 2018-10-25 18:05
 **/
@Data
public class OrderVo {
    String id;
    String time;
    String pair;
    String type;
    String price;
    String amount;
    String total;
    String executed;
    String unExecuted;
    String status;
}
