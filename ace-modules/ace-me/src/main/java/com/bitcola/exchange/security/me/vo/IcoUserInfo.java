package com.bitcola.exchange.security.me.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * ICO 用户提交资料
 *
 * @author zkq
 * @create 2018-09-29 09:52
 **/
@Data
public class IcoUserInfo {
    String firstName;
    String lastName;
    String sex;
    String birthDay;
    String email;
    String areaCode;
    String telPhone;
    BigDecimal plannedInvestment;
    String ethAddress;
    String country;
    String IDCardType;
    String frontSide;
    String backSide;
}
