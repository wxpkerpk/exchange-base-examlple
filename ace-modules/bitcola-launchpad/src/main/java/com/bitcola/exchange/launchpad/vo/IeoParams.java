package com.bitcola.exchange.launchpad.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zkq
 * @create 2019-03-14 13:40
 **/
@Data
public class IeoParams {
    String id;
    String projectId;
    String userId;
    String coinCode;
    Long start;
    Long end;
    Integer status = 0;
    String title;
    String titleImg;
    String titleCn;
    BigDecimal price;
    BigDecimal number;
    List<String> symbols = new ArrayList<>();
    Long issueTime;
    BigDecimal remain;
    BigDecimal reward = BigDecimal.ZERO;
    String introduction;
    String introductionCn;
    String symbolStr;
    BigDecimal allowMinNumber;
    BigDecimal allowMaxNumber;
    BigDecimal allowTotalNumber;
}
