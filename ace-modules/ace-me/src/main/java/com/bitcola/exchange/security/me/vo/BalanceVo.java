package com.bitcola.exchange.security.me.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户资金
 *
 * @author zkq
 * @create 2018-09-11 16:22
 **/
@Data
public class BalanceVo {

    private BigDecimal total;
    private BigDecimal change;
    private List<BalanceItemVo> balance;

    public List<BalanceItemVo> getBalance() {
        if (balance == null){
            balance = new ArrayList<>();
        }
        return balance;
    }
}
