package com.bitcola.exchange.security.admin.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2018-11-28 17:34
 **/
@Data
@Table(name = "ag_admin_v1.cola_user_reward")
public class ColaUserReward {
    @Id
    String id;
    @Column(name = "user_id")
    String userId;
    Long time;
    @Column(name = "coin_code")
    String coinCode;
    @Column(name = "action_type")
    String actionType;
    BigDecimal account;
    String status;
    String description;
}
