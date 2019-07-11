package com.bitcola.exchange.security.community.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于列表显示的内容
 *
 * @author zkq
 * @create 2018-08-22 15:01
 **/
@Data
@Document(indexName="bitcola",type="article",indexStoreType="fs",shards=5,replicas=1,refreshInterval="-1")
public class ArticleItemEntity {


    /**
     * 此 id 和文章 id 一致
     */
    @Id
    private String id;

    /**
     * "article" "shortArticle"
     */
    @Field(index = false)
    private String type;

    @Indexed
    @Field(index = false)
    private String fromUser;

    private String fromUsername;

    @Field(index = false)
    String fromUserAvatar;

    String fromUserSign;
    @Indexed
    private String language = "CN";
    /**
     * 限制字数的内容,完整的需要点进去才能看到
     */
    private String content;

    /**
     * 如果是 "article" 这里只能有一张图片
     */
    @Field(index = false)
    private List<ImageEntity> images;

    @Indexed
    @Field(index = false)
    private Long time;

    /**
     * content是否完整显示
     */
    @Field(index = false)
    private Integer isFull;

    /**
     * 权重
     */
    @Field(index = false)
    @JSONField(serialize = false)
    private Long weight;

    @Field(index = false)
    private long likeNumber;
    @Field(index = false)
    private long commentNumber;
    @Field(index = false)
    private BigDecimal donateNumber = BigDecimal.ZERO;
    @Field(index = false)
    String fromNickName;
    @Field(index = false)
    private int isLiked;
    List<CommentEntity> comments = new ArrayList<>();

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
        if (images == null) {
            images = new ArrayList<>();
        }
        return images;
    }

    public void setImages(List<ImageEntity> images) {
        this.images = images;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Integer getIsFull() {
        return isFull;
    }

    public void setIsFull(Integer isFull) {
        this.isFull = isFull;
    }

    @JSONField(serialize = false)
    public Long getWeight() {
        return weight;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
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
