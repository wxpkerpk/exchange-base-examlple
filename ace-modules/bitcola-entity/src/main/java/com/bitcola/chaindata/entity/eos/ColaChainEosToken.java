package com.bitcola.chaindata.entity.eos;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author zkq
 * @create 2018-12-28 14:50
 **/
@Data
@Table(name = "ag_admin_v1.cola_chain_eos_token")
public class ColaChainEosToken {
    @Id
    @Column(name = "coin_code")
    String coinCode;
    @Column(name = "token_name")
    String tokenName;
    String symbol;
    Integer precision;
}
