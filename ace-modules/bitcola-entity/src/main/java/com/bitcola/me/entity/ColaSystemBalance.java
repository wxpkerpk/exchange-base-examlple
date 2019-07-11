package com.bitcola.me.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 系统资金
 * @author zkq
 * @create 2018-10-25 16:11
 **/
@Data
@Table(name = "ag_admin_v1.cola_system_balance")
public class ColaSystemBalance implements Serializable {
    @Id
    String id;
    String action;
    @Column(name = "from_user")
    String fromUser;
    @Column(name = "to_user")
    String toUser;
    BigDecimal amount;
    @Column(name = "coin_code")
    String coinCode;
    Long time;
    String type;
    String description;

}
