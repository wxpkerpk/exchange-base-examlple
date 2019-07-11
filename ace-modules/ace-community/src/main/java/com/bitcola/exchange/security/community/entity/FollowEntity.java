package com.bitcola.exchange.security.community.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.persistence.Id;

/**
 * 关注关系
 *
 * @author zkq
 * @create 2018-09-17 10:12
 **/
@Data
public class FollowEntity {

    @Id
    private String id;

    /**
     * 用户 ID (粉丝,我关注别人,我是粉丝)
     */
    @Indexed
    private String userId;

    /**
     * 关注人了哪些人ID
     */
    @Indexed
    private String followUserId;

    /**
     * 时间
     */
    private long time;

}
