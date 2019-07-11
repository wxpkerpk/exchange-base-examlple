package com.bitcola.exchange.security.community.entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论详情
 *
 * @author zkq
 * @create 2018-08-18 15:52
 **/
@Data
public class CommentEntity {


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

    String fromUserSign;

    String fromNickName;

    int isLiked;

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

    private long likeNumber;

    private long commentNumber;

    List<CommentEntity> comments = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public long getLikeNumber() {
        return likeNumber;
    }

    public void setLikeNumber(long likeNumber) {
        this.likeNumber = likeNumber;
    }

    public long getCommentNumber() {
        return commentNumber;
    }

    public void setCommentNumber(long commentNumber) {
        this.commentNumber = commentNumber;
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
