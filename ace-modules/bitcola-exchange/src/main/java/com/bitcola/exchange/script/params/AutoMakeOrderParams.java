package com.bitcola.exchange.script.params;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-03-18 16:12
 **/
@Data
public class AutoMakeOrderParams {
    /**
     * 交易对
     */
    String pair;
    /**
     * 每次交易最数量量,会在交易对minNumber ~ maxNumber 之间随机成交数量 例如: 0.001 ~ 10
     */
    BigDecimal maxNumber;
    /**
     * 每次交易最数量量,会在交易对minNumber ~ maxNumber 之间随机成交数量 例如: 0.001 ~ 10
     */
    BigDecimal minNumber;
    /**
     * 每小时成交次数,会在1天内随机时间成交
     */
    int perHourTime = 60;

}
