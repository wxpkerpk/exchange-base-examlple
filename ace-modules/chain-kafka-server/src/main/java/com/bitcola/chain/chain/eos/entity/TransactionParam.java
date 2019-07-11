package com.bitcola.chain.chain.eos.entity;

import lombok.Data;

/**
 * @author zkq
 * @create 2018-12-13 10:29
 **/
@Data
public class TransactionParam {
    String signatures;
    String expiration;
    Long ref_block_num;
    Long ref_block_prefix;
}
