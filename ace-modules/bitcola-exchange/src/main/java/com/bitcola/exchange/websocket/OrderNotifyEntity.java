package com.bitcola.exchange.websocket;

import lombok.Data;

import java.math.BigDecimal;


/**
 * @author zkq
 * @create 2019-02-15 12:16
 **/
@Data
public class OrderNotifyEntity {
    BigDecimal price;
    BigDecimal number;
    String direction;
    Long timestamp;
    public OrderNotifyEntity(BigDecimal price, BigDecimal number, String direction, Long timestamp){
        this.price = price;
        this.number = number;
        this.direction = direction;
        this.timestamp = timestamp;
    }
    public OrderNotifyEntity(){}
}
