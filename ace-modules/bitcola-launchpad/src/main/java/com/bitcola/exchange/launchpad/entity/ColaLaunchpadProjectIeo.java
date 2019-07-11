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
@Table(name = "ag_admin_v1.cola_launchpad_project_ieo")
public class ColaLaunchpadProjectIeo {
    @Id
    String id;
    @Column(name = "coin_code")
    String coinCode;
    @Column(name = "user_id")
    String userId;
    /**
     * 项目 ID,此字段不做业务处理
     */
    @Column(name = "project_id")
    String projectId;
    Long start;
    @Column(name = "\"end\"")
    Long end;
    Integer status;
    String title;
    @Column(name = "title_img")
    String titleImg;
    BigDecimal price;
    BigDecimal number;
    String symbols;
    @Column(name = "issue_time")
    Long issueTime;
    BigDecimal remain;
    BigDecimal reward;
    String introduction;
    BigDecimal allowMinNumber;
    BigDecimal allowMaxNumber;
    BigDecimal allowTotalNumber;
}
