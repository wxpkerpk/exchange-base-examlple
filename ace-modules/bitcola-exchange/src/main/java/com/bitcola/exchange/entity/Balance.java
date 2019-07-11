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
@Table(name = "ag_admin_v1.cola_me_balance")
public class Balance {
    @Id
    String id;
    @Column(name = "user_id")
    String userId;
    @Column(name = "coin_code")
    String coinCode;
    @Column(name = "balance_available")
    BigDecimal balanceAvailable;
    @Column(name = "balance_frozen")
    BigDecimal balanceFrozen;
}
