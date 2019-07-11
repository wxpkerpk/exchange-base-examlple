package com.bitcola.caculate.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data

@Table(name = "ag_admin_v1.reward_log")
public class RewardLog {
    @Id
    @Column(name = "id")
    String id;
    @Column(name = "user_id")
    String userId;
    @Column(name = "coin_code")
    String coinCode;
    @Column(name = "order_id")
    String orderId;
    @Column(name = "count")
    BigDecimal count;
    @Column(name = "time")
    long time=0l;
}
