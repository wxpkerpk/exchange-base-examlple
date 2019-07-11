package com.bitcola.chain.chain.tera.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-03-08 09:51
 **/
@Data
public class History {
    long Type;
    long BlockNum;
    long TrNum;
    long Pos;
    String Direct;
    long CorrID;
    BigDecimal SumCOIN;
    BigDecimal SumCENT;
    String TxID;
    String Description;
}
