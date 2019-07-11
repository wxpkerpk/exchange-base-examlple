package com.bitcola.exchange.security.common.msg;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-01-21 12:15
 **/
@Data
@Table(name = "bitcola_chain.cola_chain_deposit")
public class ColaChainDepositResponse implements Serializable {
    @Id
    String hash;
    BigDecimal amount;
    Long timestamp;
    @Column(name = "coin_code")
    String coinCode;
    String module;
    String status;
    @Column(name = "to_address")
    String toAddress;
    String memo;
    @Column(name = "order_id")
    String orderId;
}
