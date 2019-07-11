package com.bitcola.exchange.security.community.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author lky
 * @create 2019-04-26 11:00
 **/
@Data
public class FromDonateVo {

    /**
     * 用户ID
     */
    String fromDonate;

    /**
     * 打赏金额
     */
    BigDecimal donateNumber;

    /**
     * 打赏币种
     */
    String donateCoinCode;

    public FromDonateVo(String fromDonate, BigDecimal donateNumber, String donateCoinCode) {
        this.fromDonate = fromDonate;
        this.donateNumber = donateNumber;
        this.donateCoinCode = donateCoinCode;
    }
}
