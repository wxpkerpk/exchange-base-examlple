package com.bitcola.chain.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author zkq
 * @create 2019-01-22 19:55
 **/
@Data
@Table(name = "bitcola_chain.cola_chain_key")
public class ColaChainKey {
    @Id
    String module;
    String key;
    String description;
}
