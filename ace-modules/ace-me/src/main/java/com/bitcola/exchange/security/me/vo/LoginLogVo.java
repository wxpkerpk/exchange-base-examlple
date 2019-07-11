package com.bitcola.exchange.security.me.vo;

import lombok.Data;

/**
 * @author zkq
 * @create 2018-10-11 12:37
 **/
@Data
public class LoginLogVo {

    int index;
    Long time;
    String ip;
    String status;
    String loginMethod;
    String address;
}
