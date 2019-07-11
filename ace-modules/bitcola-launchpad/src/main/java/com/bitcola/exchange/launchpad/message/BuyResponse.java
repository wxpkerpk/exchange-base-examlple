package com.bitcola.exchange.launchpad.message;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-03-15 15:55
 **/
@Data
public class BuyResponse {
    boolean success; // 是否下单成功
    BigDecimal number; // 购买了多少个
    int errorCode; // 错误码
    String errorMsg; // 错误原因,这里返回国际化前的 key
    public BuyResponse(boolean success,BigDecimal number,int errorCode,String errorMsg){
        this.success = success;
        this.number = number;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;

    }
}
