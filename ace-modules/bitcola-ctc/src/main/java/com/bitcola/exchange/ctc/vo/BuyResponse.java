package com.bitcola.exchange.ctc.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-05-09 12:05
 **/
@Data
public class BuyResponse {
    @JsonIgnore
    @JSONField(serialize = false)
    boolean success;
    @JsonIgnore
    @JSONField(serialize = false)
    String errorMsg;

    String coinCode;
    BigDecimal price;
    BigDecimal number;
    String memo;
    String status;
    String payUserName;
    String payBankName;
    String payBankAddress;
    String payCardId;
    String payCardIcon;
    BigDecimal amount;
    BigDecimal fee;

}
