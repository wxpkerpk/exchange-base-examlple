package com.bitcola.exchange.script.params;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-03-19 15:27
 **/
@Data
public class DynamicDepthParams {
    String pair;
    int perMinTime = 20;
    BigDecimal maxNumber;
    BigDecimal minNumber;
}
