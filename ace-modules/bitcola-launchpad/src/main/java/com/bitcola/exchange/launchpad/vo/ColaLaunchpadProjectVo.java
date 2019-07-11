package com.bitcola.exchange.launchpad.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zkq
 * @create 2019-03-13 18:41
 **/
@Data
public class ColaLaunchpadProjectVo {
    String id;
    String title;
    String titleImg;
    String introduction;
    String status;
    BigDecimal price;
    BigDecimal currentSupply;
    Long currentTime;
    Long start;
    Long end;
    BigDecimal reward;
    List<String> symbol;
    String coinCode;
    BigDecimal totalSupply;
    String application;
    String website;
    String whitePaper;
    String platform;
    List<Community> community;
    Long issueTime;
    List<String> detail;
    BigDecimal remain;
    BigDecimal allowMinNumber;
    BigDecimal allowMaxNumber;
}
