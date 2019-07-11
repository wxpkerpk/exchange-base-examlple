package com.bitcola.exchange.script.params;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-03-19 16:57
 **/
@Data
public class BalanceCoinPriceParams {
    String pair;
    double binanceWeight = 1;
    double gateioWeight = 1;
    double huobiWeight = 1;
    boolean bitcola = false; // 是否依照 bitcola 平台来平衡价格
    BigDecimal rate = new BigDecimal("0.001"); // 比率,超过主流平台加权平均价的比率的订单会被吃掉
    BigDecimal safeRate = new BigDecimal("0.001"); // 安全阈值 ,在安全阈值比例内的订单全部取消掉
    BigDecimal safeNumber; // 安全数量,在安全阈值内,超过这个数量的挂单将被取消,以免恶意拉盘砸盘
    BigDecimal supplyOrderRate = new BigDecimal("0.0003"); // 按照这个比例来补齐订单

    public String coinCode(){
        return this.pair.split("_")[0];
    }
    public String symbol(){
        return this.pair.split("_")[1];
    }
}
