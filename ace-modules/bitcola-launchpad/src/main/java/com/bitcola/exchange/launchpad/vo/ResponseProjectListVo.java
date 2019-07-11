package com.bitcola.exchange.launchpad.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-05-24 17:39
 **/
@Data
public class ResponseProjectListVo {
    String coinCode;
    String symbol;
    String title;
    String titleImage;
    String introduction;
    String status;

    BigDecimal capitalPool;
    BigDecimal conversionRatio;
    Long startTime;
    Long timestamp;
    Long endTime;
    BigDecimal countNumber;
    BigDecimal total;
    BigDecimal remain;
    Integer round;
    BigDecimal capitalPoolCoinCode;
}
