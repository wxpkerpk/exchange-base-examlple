package com.bitcola.exchange.launchpad.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.bitcola.exchange.launchpad.vo.Community;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zkq
 * @create 2019-03-13 19:10
 **/
@Data
public class ColaLaunchpadProjectDto {
    String id;
    String title;
    String titleImg;
    String titleCn;
    String introduction;
    String introductionCn;
    String coinCode;
    Integer status;
    Long start;
    Long end;
    BigDecimal currentSupply;
    BigDecimal reward;
    String symbol;
    BigDecimal price;
    BigDecimal totalSupply;
    String application;
    String website;
    String whitePaper;
    String platform;
    String community;
    Long issueTime;
    String detail;
    String detailCn;
    BigDecimal remain;
    BigDecimal allowMinNumber;
    BigDecimal allowMaxNumber;
}
