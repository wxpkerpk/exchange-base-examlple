package com.bitcola.chain.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-04-05 16:52
 **/
@Data
@Table(name = "bitcola_chain.cola_chain_coin")
public class ColaChainCoin {
    @Id
    @Column(name = "coin_code")
    String coinCode;
    @Column(name = "confirm_number")
    Integer confirmNumber;
    @Column(name = "deposit_min")
    BigDecimal depositMin;
    String module;
    @Column(name = "transfer_to_hot_limit")
    BigDecimal transferToHotLimit;
}
