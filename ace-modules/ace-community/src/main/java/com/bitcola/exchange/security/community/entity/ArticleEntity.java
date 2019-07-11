package com.bitcola.exchange.security.community.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 文章
 *
 * @author zkq
 * @create 2018-08-18 16:27
 **/
@Data
public class ArticleEntity {

    @Id
    private String id;

    @Indexed
    private String fromUser;

    private String fromUsername;

    String fromUserAvatar;

    String fromUserSign;

    private long likeNumber;

    private long commentNumber;

    private BigDecimal donateNumber;

    private int isLiked;
    @Indexed
    private String language = "CN";

    /**
     * 标题
     */
    private String title;

    /**
     * 封面图片
     */
    private ImageEntity titleImage;

    /**
     * 发布时间
     */
    @Indexed
    private long time;

    /**
     * 按照顺序包含文字和图片地址
     */
    Object content;

    String fromNickName;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ImageEntity getTitleImage() {
        return titleImage;
    }

    public void setTitleImage(ImageEntity titleImage) {
        this.titleImage = titleImage;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
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
