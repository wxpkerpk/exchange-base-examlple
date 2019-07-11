package com.bitcola.chain.chain.eos.entity;

import lombok.Data;

import java.util.List;

/**
 * @author zkq
 * @create 2018-12-08 17:28
 **/
@Data
public class EosTransactions {
    Integer trace_count;
    List<EosTransaction> trace_list;
}
