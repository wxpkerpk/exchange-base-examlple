package com.bitcola.chain.chain.stellar;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-01-17 16:14
 **/
@Data
public class XlmTransaction {
    String hash;
    String from;
    String to;
    BigDecimal amount;
    String memo;
    Boolean isToken;
    String tokenCode;
    String tokenIssuer;
}
