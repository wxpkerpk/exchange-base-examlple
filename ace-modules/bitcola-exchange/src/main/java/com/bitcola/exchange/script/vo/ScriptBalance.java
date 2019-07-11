package com.bitcola.exchange.script.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zkq
 * @create 2019-03-29 16:05
 **/
@Data
public class ScriptBalance {
    BigDecimal aTotal;
    BigDecimal bTotal;
    BigDecimal total;
    List<BalanceDetail> balanceDetail = new ArrayList<>();
}
