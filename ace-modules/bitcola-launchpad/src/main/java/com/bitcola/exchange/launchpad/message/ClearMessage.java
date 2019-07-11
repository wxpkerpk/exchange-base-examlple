package com.bitcola.exchange.launchpad.message;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-03-14 17:57
 **/
@Data
public class ClearMessage {
    String id;
    String userId; //   - symbol  coin放入指定账户,指定时间发放 coin
    String projectUserId; // 从这里面扣除币种 - coin  symbol 放入指定账户,指定时间发放 symbol
    String coinCode;
    String symbol;
    BigDecimal price; // 非美元价格
    BigDecimal number;
    BigDecimal buyRealNumber; // 时间成交数量
    long timestamp;
    BigDecimal reward;

    public ClearMessage(String id,String userId,String projectUserId,String coinCode,String symbol,  BigDecimal price,BigDecimal number,BigDecimal buyRealNumber,long timestamp,BigDecimal reward){
        this.id = id;
        this.userId = userId;
        this.projectUserId = projectUserId;
        this.coinCode = coinCode;
        this.symbol = symbol;
        this.price = price;
        this.number = number;
        this.buyRealNumber = buyRealNumber;
        this.timestamp = timestamp;
        this.reward = reward;
    }
}
