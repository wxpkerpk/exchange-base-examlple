package com.bitcola.exchange.security.admin.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-01-30 19:38
 **/
@Data
@Table(name = "ag_admin_v1.cola_virtual_asset")
public class ColaVirtualAsset {
    String id;
    @Column(name = "coin_code")
    String coinCode;
    @Column(name = "to_user")
    String toUser;
    Long timestamp;
    BigDecimal number;
    String description;
}
