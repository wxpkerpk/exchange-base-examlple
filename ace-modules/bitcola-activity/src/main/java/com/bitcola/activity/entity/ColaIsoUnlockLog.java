package com.bitcola.activity.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-05-18 09:56
 **/
@Data
public class ColaIsoUnlockLog {
    @Id
    String id;
    @Column(name = "coin_code")
    String coinCode;
    BigDecimal number;
    String type;
    @Column(name = "user_id")
    String userId;
    @Column(name = "user_stage")
    Integer userStage;
    @Column(name = "inviter_stage")
    Integer inviterStage;
    Long timestamp;
}
