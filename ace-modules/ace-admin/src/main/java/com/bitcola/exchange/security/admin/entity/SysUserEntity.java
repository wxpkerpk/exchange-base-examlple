package com.bitcola.exchange.security.admin.entity;

import lombok.Data;

/**
 * @author zkq
 * @create 2018-11-27 12:02
 **/
@Data
public class SysUserEntity {
    String id;
    String username;
    String nickName;
    String telephone;
    String email;
    Long signUpTime;
    boolean kyc;
}
