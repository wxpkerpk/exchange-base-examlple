package com.bitcola.chain.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigInteger;

/**
 * @author zkq
 * @create 2019-01-22 19:55
 **/
@Data
@Table(name = "bitcola_chain.cola_chain_eth_key")
public class ColaChainEthKey {
    @Id
    String address;
    @Column(name = "private_key")
    String privateKey;
    @Column(name = "public_key")
    String publicKey;
}
