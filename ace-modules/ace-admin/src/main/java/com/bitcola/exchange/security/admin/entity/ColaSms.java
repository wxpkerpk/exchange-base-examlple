package com.bitcola.exchange.security.admin.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author zkq
 * @create 2019-05-09 14:45
 **/
@Data
@Table(name = "ag_admin_v1.cola_sms")
public class ColaSms {
    @Id
    String id;
    String telephone;
    String username;
    String module;
}
