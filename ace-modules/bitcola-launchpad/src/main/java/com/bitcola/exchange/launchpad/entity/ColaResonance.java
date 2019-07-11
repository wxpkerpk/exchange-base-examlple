package com.bitcola.exchange.launchpad.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-05-12 12:01
 **/
@Data
public class ColaResonance {
    @Id
    String id;
    @Column(name = "user_id")
    String userId;
    BigDecimal price;
    BigDecimal number;
    BigDecimal amount;
    Long timestamp;
    @Column(name = "coin_code")
    String coinCode;
    String symbol;
    Integer round;
    Integer stage;
}
