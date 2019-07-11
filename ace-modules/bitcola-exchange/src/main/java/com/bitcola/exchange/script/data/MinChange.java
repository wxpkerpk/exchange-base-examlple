package com.bitcola.exchange.script.data;


import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 1 分钟涨跌幅
 *
 * @author zkq
 * @create 2019-03-27 12:15
 **/
public class MinChange {
    private BigDecimal minPrice = BigDecimal.ZERO; // 1分钟前的价格
    private long timestamp = 0; // 1分钟的刻度线

    public BigDecimal getMinChange(BigDecimal currentPrice) {
        if (this.minPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentPrice.subtract(this.minPrice).divide(this.minPrice, 4, RoundingMode.HALF_UP);
    }

    public void put(BigDecimal price) {
        if (System.currentTimeMillis() - this.timestamp > 60 * 1000) {
            this.timestamp = System.currentTimeMillis();
            this.minPrice = price;
        }
    }

}
