package com.bitcola.chain.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-01-24 17:43
 **/
@Data
@Table(name = "bitcola_chain.cola_chain_withdraw")
public class ColaChainWithdraw {
    @Id
    @Column(name = "order_id")
    String orderId;
    @Column(name = "coin_code")
    String coinCode;
    String module;
    String address;
    BigDecimal number;
    String memo;
    String status;
    BigDecimal fee;
    String hash;
    @Column(name = "fee_coin_code")
    String feeCoinCode;
    String error;
}
