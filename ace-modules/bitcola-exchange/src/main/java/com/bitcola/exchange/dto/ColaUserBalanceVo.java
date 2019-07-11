package com.bitcola.exchange.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-02-16 13:30
 **/
@Data
public class ColaUserBalanceVo {
    String coinCode;
    BigDecimal balanceAvailable;
    BigDecimal balanceFrozen;
    BigDecimal worth;
}
