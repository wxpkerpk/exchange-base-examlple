package com.bitcola.exchange.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-02-22 14:34
 **/
@Data
public class BatchBalance {
    String id;
    BigDecimal available;
    BigDecimal frozen;
    public BatchBalance(String id){
        this.id = id;
        this.available = BigDecimal.ZERO;
        this.frozen = BigDecimal.ZERO;
    }
}
