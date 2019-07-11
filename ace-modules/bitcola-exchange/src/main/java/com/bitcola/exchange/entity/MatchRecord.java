package com.bitcola.exchange.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-02-13 10:50
 **/
@Data
@Table(name = "ag_admin_v1.cola_exchange_match_record")
public class MatchRecord {
    @Id
    String id;
    @Column(name = "order_id")
    String orderId;
    String type;
    BigDecimal price;
    BigDecimal number;
    long timestamp;
    String pair;
    BigDecimal fee;
    @Column(name = "fee_coin_code")
    String feeCoinCode;
    String direction;
    String dump;
    @Column(name = "user_id")
    String userId;
    public String coinCode(){
        return this.pair.split("_")[0];
    }
    public String symbol(){
        return this.pair.split("_")[1];
    }
}
