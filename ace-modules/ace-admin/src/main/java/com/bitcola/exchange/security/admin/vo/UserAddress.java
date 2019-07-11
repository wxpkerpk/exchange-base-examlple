package com.bitcola.exchange.security.admin.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-05-21 11:18
 **/
@Data
public class UserAddress {
    String userId;
    String id;
    String coinCode;
    String address;
    BigDecimal available;
    BigDecimal frozen;
}
