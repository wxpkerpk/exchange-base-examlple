package com.bitcola.exchange.security.community.vo;

import lombok.Data;

/**
 * @author zkq
 * @create 2018-09-17 16:21
 **/
@Data
public class UserVo {

    /**
     * 系统关联 ID
     */
    String sysUserID;

    /**
     * 用户昵称
     */
    String username;

    /**
     * 个性签名
     */
    String sign;

    /**
     * 头像
     */
    String avatar;

    /**
     * 昵称
     */
    String nickName;
}
