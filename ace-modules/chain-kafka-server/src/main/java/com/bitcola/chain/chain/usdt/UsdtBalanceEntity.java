package com.bitcola.chain.chain.usdt;

import lombok.Data;

import java.util.List;

/**
 * @author zkq
 * @create 2019-01-29 15:09
 **/
@Data
public class UsdtBalanceEntity {
    String address;
    List<Balance> balances;
}
