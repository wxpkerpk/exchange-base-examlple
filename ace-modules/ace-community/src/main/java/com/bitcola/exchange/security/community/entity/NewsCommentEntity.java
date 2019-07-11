package com.bitcola.exchange.security.community.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

/**
 * @author lky
 * @create 2019-04-24 12:40
 **/
@Data
public class NewsCommentEntity {
    @Id
    private String id;

    /**
     * 上级 ID
     */
    @Indexed
    private String parentId;

    /**
     * 内容
     */
    private String content;

    /**
     * 发表人
     */
    @Indexed
    private String fromUser;

    /**
     * 发表人名称
     */
    private String fromUsername;

    String fromUserAvatar;

    String fromNickName;

    private Long time;
}
