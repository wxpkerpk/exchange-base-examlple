package com.bitcola.exchange.security.community.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;


/**
 * 点赞详情
 *
 * @author zkq
 * @create 2018-08-18 16:22
 **/
@Data
public class LikeEntity {



    @Id
    private String id;

    /**
     * 上级 ID
     */
    @Indexed
    private String parentId;

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

    String fromUserSign;

    String fromNickName;

    public String getFromNickName() {
        return fromNickName;
    }

    public void setFromNickName(String fromNickName) {
        this.fromNickName = fromNickName;
    }

    /**
     * 发表时间
     */
    @Indexed
    private Long time;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getFromUserAvatar() {
        return fromUserAvatar;
    }

    public void setFromUserAvatar(String fromUserAvatar) {
        this.fromUserAvatar = fromUserAvatar;
    }

    public String getFromUserSign() {
        return fromUserSign;
    }

    public void setFromUserSign(String fromUserSign) {
        this.fromUserSign = fromUserSign;
    }
}
