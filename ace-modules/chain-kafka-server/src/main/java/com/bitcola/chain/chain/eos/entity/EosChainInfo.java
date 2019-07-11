package com.bitcola.chain.chain.eos.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author zkq
 * @create 2018-12-10 15:42
 **/
@Data
public class EosChainInfo {
    @JSONField(format = "head_block_num")
    private String headBlockNum;
    private String chain_id;
}
