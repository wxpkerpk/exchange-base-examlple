package com.bitcola.exchange.launchpad.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-03-13 18:57
 **/
@Data
@Table(name = "ag_admin_v1.cola_launchpad_project")
public class ColaLaunchpadProject {
    @Id
    String id;
    @Column(name = "coin_code")
    String coinCode;
    Long timestamp;
    @Column(name = "user_id")
    String userId;
    @Column(name = "total_supply")
    BigDecimal totalSupply;
    String application;
    String website;
    @Column(name = "white_paper")
    String whitePaper;
    String platform;
    String community;
    String detail;
}
