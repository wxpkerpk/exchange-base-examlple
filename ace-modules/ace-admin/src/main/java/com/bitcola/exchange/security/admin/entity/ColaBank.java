package com.bitcola.exchange.security.admin.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author zkq
 * @create 2019-05-09 15:48
 **/
@Data
@Table(name = "ag_admin_v1.cola_bank")
public class ColaBank {
    @Id
    String id;
    @Column(name = "bank_name")
    String bankName;
    String icon;
    @Column(name = "white_icon")
    String whiteIcon;


}
