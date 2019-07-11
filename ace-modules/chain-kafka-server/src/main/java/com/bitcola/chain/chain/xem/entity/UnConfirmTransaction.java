package com.bitcola.chain.chain.xem.entity;

import lombok.Data;

/**
 * @author zkq
 * @create 2018-11-23 21:38
 **/
@Data
public class UnConfirmTransaction {
    Hash meta;
    Transaction transaction;
}
