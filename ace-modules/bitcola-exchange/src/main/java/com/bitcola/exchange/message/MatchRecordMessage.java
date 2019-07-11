package com.bitcola.exchange.message;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-02-13 10:43
 **/
@Data
public class MatchRecordMessage {
    String takerOrderId;
    String makerOrderId;
    BigDecimal matchPrice;
    BigDecimal matchNumber;
    String makerStatus;

    public MatchRecordMessage(String takerOrderId, String makerOrderId, BigDecimal matchPrice, BigDecimal matchNumber, String makerStatus){
        this.takerOrderId = takerOrderId;
        this.makerOrderId = makerOrderId;
        this.matchPrice = matchPrice;
        this.matchNumber = matchNumber;
        this.makerStatus = makerStatus;
    }

}
