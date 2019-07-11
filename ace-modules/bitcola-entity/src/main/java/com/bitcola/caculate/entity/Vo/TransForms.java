package com.bitcola.caculate.entity.Vo;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class TransForms{
    String userId;
    BigDecimal from;
    BigDecimal to;
    String fromCode;
    String toCode;

    public TransForms(String userId, BigDecimal from, BigDecimal to, String fromCode, String toCode) {
        this.userId = userId;
        this.from = from;
        this.to = to;
        this.fromCode = fromCode;
        this.toCode = toCode;
    }
}