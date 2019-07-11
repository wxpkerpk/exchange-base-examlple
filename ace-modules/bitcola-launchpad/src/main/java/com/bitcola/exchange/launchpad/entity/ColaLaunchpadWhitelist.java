package com.bitcola.exchange.launchpad.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author zkq
 * @create 2019-03-13 15:45
 **/
@Data
@Table(name = "ag_admin_v1.cola_launchpad_whitelist")
public class ColaLaunchpadWhitelist {
    @Id
    @Column(name = "user_id")
    String userId;
    String status;
    String reason;
    String detail;
}
