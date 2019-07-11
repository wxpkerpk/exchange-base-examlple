package com.bitcola.exchange.security.me.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zkq
 * @create 2018-10-25 10:25
 **/
@Data
public class FinancialRecordsDto implements Serializable {
    String userId;
    String asset;
    String action;
    Long startTime;
    Long endTime;
    Integer excludeInviteRewards;
    String keyWord;
    Integer limit;
    Integer page;

}
