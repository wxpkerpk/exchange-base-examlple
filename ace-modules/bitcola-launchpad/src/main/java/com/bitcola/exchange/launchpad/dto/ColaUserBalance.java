package com.bitcola.exchange.launchpad.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-03-14 20:28
 **/
@Data
public class ColaUserBalance {
    String id;
    BigDecimal available;
    BigDecimal frozen;
    public ColaUserBalance(String id){
        this.id = id;
        this.available = BigDecimal.ZERO;
        this.frozen = BigDecimal.ZERO;
    }
}
