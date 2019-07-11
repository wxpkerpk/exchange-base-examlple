package com.bitcola.exchange.security.me.vo;

import lombok.Data;

/**
 * 邀请好友列表
 *
 * @author zkq
 * @create 2018-10-21 15:30
 **/
@Data
public class InvitationVo {

    String userId;

    String username;

    Long time;
}
