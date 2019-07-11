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
public class ProjectParams {
    String id;
    Long timestamp;
    String coinCode;
    String userId;
    BigDecimal totalSupply;
    String application;
    String website;
    String whitePaper;
    String platform;
    List<Community> community = new ArrayList<>();
    List<String> detail = new ArrayList<>();
    List<String> detailCn = new ArrayList<>();
    String communityStr;
    String detailStr;
    String detailCnStr;
}
