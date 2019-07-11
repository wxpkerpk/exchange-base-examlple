package com.bitcola.dataservice.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-03-04 16:47
 **/
@Data
public class WithdrawDto {
    String coinCode;
    String orderId;
    String address;
    String memo;
    BigDecimal number;
    BigDecimal realNumber;
    String coinBelong;
    String userId;
    String userTelephone;
    String userAreaCode;
    String userEmail;
    String antiPhishingCode;
    BigDecimal fee; // 扣除用户的手续费
    String language;
}
