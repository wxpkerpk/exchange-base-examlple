package com.bitcola.exchange.security.community.entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 短文
 * @author zkq
 * @create 2018-08-18 16:29
 **/
@Data
public class ShortArticleEntity {

    @Id
    String id;

    @Indexed
    Long time;

    @Indexed
    String fromUser;
    @Indexed
    private String language = "CN";

    String fromUsername;

    String fromUserAvatar;

    String fromUserSign;

    String content;

    List<ImageEntity> images;

    String fromNickName;

    private long likeNumber;

    private long commentNumber;

    private BigDecimal donateNumber;

    private int isLiked;

    public String getFromNickName() {
        return fromNickName;
    }

    public void setFromNickName(String fromNickName) {
        this.fromNickName = fromNickName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<ImageEntity> getImages() {
        if (images == null){
            images = new ArrayList<>();
        }
        return images;
    }

    public void setImages(List<ImageEntity> images) {
        this.images = images;
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
