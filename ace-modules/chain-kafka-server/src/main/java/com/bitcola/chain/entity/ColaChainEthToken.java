package com.bitcola.chain.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author zkq
 * @create 2019-04-28 10:42
 **/
@Data
@Table(name = "bitcola_chain.cola_chain_eth_token")
public class ColaChainEthToken {
    @Id
    @Column(name = "coin_code")
    String coinCode;
    String contract;
    Integer unit;
}
