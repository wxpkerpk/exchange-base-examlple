package com.bitcola.exchange.launchpad.entity;

import lombok.Data;

import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-05-17 14:49
 **/
@Data
@Table(name = "ag_admin_v1.cola_launchpad_resonance_destroy")
public class ColaResonanceDestroy {
    String id;
    BigDecimal number;
    Long timestamp;
    String coinCode;
    int stage;
}
