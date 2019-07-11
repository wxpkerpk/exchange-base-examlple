package com.bitcola.chain.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author zkq
 * @create 2019-01-21 12:15
 **/
@Data
@Table(name = "bitcola_chain.cola_chain_module")
public class ColaChainModule {
    @Id
    String module;
    String status;
    Long timestamp;
    String host;
    Integer maintain;// 0 表示在维护
}
