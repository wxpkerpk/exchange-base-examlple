package com.bitcola.chain.chain.xem.entity;

import lombok.Data;

/**
 * @author zkq
 * @create 2018-11-26 11:54
 **/
@Data
public class XemWithdraw {
    Transaction transaction;
    String privateKey;
}
