package com.bitcola.exchange.launchpad.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-03-14 16:22
 **/
@Data
public class BuyParams {
    String id;
    String pin;
    String ticket; // 腾讯防水墙验证
    String rand; // 腾讯防水墙验证
    String userIp; // 腾讯防水墙验证
    BigDecimal number;
    String symbol;
}
