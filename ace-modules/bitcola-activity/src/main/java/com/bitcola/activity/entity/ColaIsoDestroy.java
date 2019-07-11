package com.bitcola.activity.entity;

import lombok.Data;

import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-05-17 14:49
 **/
@Data
@Table(name = "ag_admin_v1.cola_activity_iso_destroy")
public class ColaIsoDestroy {
    String id;
    BigDecimal number;
    Long timestamp;
    int stage;
}
