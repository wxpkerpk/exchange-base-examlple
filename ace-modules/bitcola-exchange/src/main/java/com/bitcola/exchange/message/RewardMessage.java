package com.bitcola.exchange.message;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zkq
 * @create 2019-02-18 14:59
 **/
@Data
public class RewardMessage {
    BigDecimal fee;
    String feeCoinCode;
    String userId;
    List<RewardMessage> maker = new ArrayList<>();
    public RewardMessage(){}
    public RewardMessage(BigDecimal fee, String feeCoinCode, String userId){
        this.fee = fee;
        this.feeCoinCode = feeCoinCode;
        this.userId = userId;
    }
}
