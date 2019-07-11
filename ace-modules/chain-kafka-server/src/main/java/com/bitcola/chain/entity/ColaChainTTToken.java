package com.bitcola.chain.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-01-22 19:55
 **/
@Data
@Table(name = "bitcola_chain.cola_chain_tt_token")
public class ColaChainTTToken {
    @Id
    @Column(name = "coin_code")
    String coinCode;
    @Column(name = "contract")
    String contract;
    @Column(name = "min_auth_transfer_to_hot")
    BigDecimal minAutoTransferToHot;
}
