package com.bitcola.exchange.security.me.vo;

import lombok.Data;

/**
 * 用户个人信息
 *
 * @author zkq
 * @create 2018-09-17 20:17
 **/
@Data
public class UserInfoVo {
    private String userId;
    private String username;
    private String nickName;
    private String sign;
    private String avatar;
    private int isFollowed;
    private int isFriend;
    private long posts;
    private long followers;
    private long following;

}
