package com.bitcola.exchange.launchpad.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-03-13 19:10
 **/
@Data
public class ColaLaunchpadProjectList {
    String id;
    String title;
    String titleImg;
    String introduction;
    @JsonIgnore
    @JSONField(serialize = false)
    String introductionCn;
    String coinCode;
    BigDecimal number;
    Long start;
    Long end;
    String status;
    @JsonIgnore
    @JSONField(serialize = false)
    Integer dbStatus;
    BigDecimal reward;
    @JSONField(serialize = false)
    String titleCn;
}
