package com.bitcola.chain.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-05-22 10:05
 **/
@Data
public class ColaSmsEarlyWarning {
    String contract;
    BigDecimal threshold;
    String smsGroup;
    Integer decimal;
}
