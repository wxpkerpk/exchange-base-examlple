package com.bitcola.exchange.launchpad.vo;

import lombok.Data;

import java.util.Map;

/**
 * @author zkq
 * @create 2019-03-13 16:24
 **/
@Data
public class ColaLaunchpadApplyVo {
    String userId;
    String status;
    String reason;
    Map<String,String> detail;
}
