package com.bitcola.ctc;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-05-08 17:09
 **/
@Data
@Table(name = "ag_admin_v1.cola_ctc_limit")
public class ColaCtcLimit {

    @Id
    @Column(name = "user_id")
    String userId;
    @Column(name = "buy_limit")
    Integer buyLimit;
    @Column(name = "sell_limit")
    Integer sellLimit;
    @Column(name = "buy_limit_min")
    BigDecimal buyLimitMin;
    @Column(name = "sell_limit_min")
    BigDecimal sellLimitMin;
    @Column(name = "buy_limit_max")
    BigDecimal buyLimitMax;
    @Column(name = "sell_limit_max")
    BigDecimal sellLimitMax;



}
