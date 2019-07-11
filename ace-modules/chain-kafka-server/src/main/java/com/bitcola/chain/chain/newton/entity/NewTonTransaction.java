package com.bitcola.chain.chain.newton.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 *
 * blockhash: "0xd908fc89f698731d7bc588ec7269031683050e1e3de5c5aeda349c9035cda86c"
 * blockheight: 3226557
 * blocktime: 1554786421
 * confirmations: "2043"
 * data: "0x"
 * fees: "21000000000000"
 * fees_price: "1000000000"
 * from_addr: "NEW182GFiDqAdUXFgVRAg88Csqvf8MAoiqCPCdK"
 * from_address: "0x233b5f2f280135596058e1065c39baf42d97daef"
 * from_contract: false
 * locktime: 0
 * size: 0
 * time: 1554786421
 * to_addr: "NEW182GU6MDot1wfVC1aVux69vfuzNyCUnYkNik"
 * to_address: "0x2592d59aeb560204b27ed22ff421cd2516dfb96f"
 * to_contract: false
 * transaction_index: 2
 * txid: "0xb7216c04e6453e0639f7b4fb8c67e49c868e3cfa6a5685523014af629fb7375d"
 * value: "100"
 * valueOut: 100
 * version: 0
 * _id: "0xb7216c04e6453e0639f7b4fb8c67e49c868e3cfa6a5685523014af629fb7375d"
 *
 * @author zkq
 * @create 2019-04-09 14:53
 **/
@Data
public class NewTonTransaction {
    String txid;
    String from_addr;
    String to_addr;
    BigDecimal confirmations;
    BigDecimal fees;
    boolean from_contract;
    boolean to_contract;
    BigDecimal value;
    String data;
}
