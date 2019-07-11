package com.bitcola.caculate.entity.Vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Payback
{
    public Payback(String userId, BigDecimal count, String coinCode) {
        this.userId = userId;
        this.count = count;
        this.coinCode = coinCode;
    }

    String userId;
    BigDecimal count;
    String coinCode;
}