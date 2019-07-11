package com.bitcola.activity.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-05-18 18:02
 **/
@Data
@Table(name = "ag_admin_v1.cola_activity_iso_reward")
public class ColaIsoInviterRewardLog {
    @Id
    String id;
    Long timestamp;
    @Column(name = "user_id")
    String userId;
    @Column(name = "inviter_user_id")
    String inviterUserId; // 邀请人
    BigDecimal amount;
    BigDecimal reward;
    String symbol;
    @Column(name = "coin_code")
    String coinCode;
}
