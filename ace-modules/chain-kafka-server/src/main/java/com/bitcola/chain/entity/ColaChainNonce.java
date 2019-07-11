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
@Table(name = "bitcola_chain.cola_chain_eth_nonce")
public class ColaChainNonce {
    @Id
    @Column(name = "module")
    String module;
    Integer nonce;
    Long timestamp;
}
