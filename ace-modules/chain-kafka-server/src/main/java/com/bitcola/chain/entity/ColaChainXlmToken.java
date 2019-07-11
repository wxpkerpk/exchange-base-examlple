package com.bitcola.chain.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author zkq
 * @create 2019-01-24 18:29
 **/
@Data
@Table(name = "bitcola_chain.cola_chain_xlm_token")
public class ColaChainXlmToken {
    @Id
    @Column(name = "coin_code")
    String coinCode;
    @Column(name = "token_code")
    String tokenCode;
    @Column(name = "token_issuer")
    String tokenIssuer;
}
