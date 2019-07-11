package com.bitcola.chain.chain.nxt.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-03-12 10:38
 **/
@Data
public class Transaction {
    Attachment attachment;
    String transaction; // tx_id
    String senderRS;
    String recipientRS;
    int confirmations;
    BigDecimal amountNQT;
    BigDecimal feeNQT;
}
