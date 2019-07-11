package com.bitcola.activity.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-05-18 09:59
 **/
@Data
public class ColaIsoStage {
    @Id
    String id;
    Integer stage;
    @Column(name = "user_id")
    String userId;
    BigDecimal number;
    BigDecimal amount;
    Long timestamp;
    @Column(name = "coin_code")
    String coinCode;
    String symbol;
    @Column(name = "unlock_number")
    BigDecimal unlockNumber;

}
