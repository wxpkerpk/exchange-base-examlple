package com.bitcola.exchange.launchpad.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-03-14 20:18
 **/
@Data
@Table(name = "ag_admin_v1.cola_launchpad_exchange_log")
public class ColaLaunchpadExchangeLog {
    String id;
    @Column(name = "project_id")
    String projectId;
    @Column(name = "user_id")
    String userId;
    @Column(name = "coin_code")
    String coinCode;
    BigDecimal price;
    BigDecimal number;
    String status;
    String symbol;
    Long timestamp;
    BigDecimal reward;
}
