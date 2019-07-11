package com.bitcola.chain.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author zkq
 * @create 2019-03-08 12:49
 **/
@Data
@Table(name = "bitcola_chain.cola_chain_tera_key")
public class ColaChainTeraKey {
    @Id
    @Column(name = "account_id")
    String accountId;
    @Column(name = "private_key")
    String privateKey;
    @Column(name = "public_key")
    String publicKey;
}
