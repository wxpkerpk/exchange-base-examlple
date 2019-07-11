package com.bitcola.exchange.security.admin.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author zkq
 * @create 2018-11-29 11:15
 **/
@Data
@Table(name = "ag_auth_v1.system_maintain")
public class SystemMaintain {
    @Id
    String module;
    String status;
    Long timestamp;
}
