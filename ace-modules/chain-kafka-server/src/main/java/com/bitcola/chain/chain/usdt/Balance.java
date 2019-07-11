package com.bitcola.chain.chain.usdt;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-01-29 15:10
 **/
@Data
public class Balance {
    int propertyid;
    String name;
    BigDecimal balance;
    BigDecimal reserved;
    BigDecimal frozen;
}
