package com.bitcola.exchange.security.community.entity;

import com.bitcola.exchange.security.community.vo.FromDonateVo;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lky
 * @create 2019-04-23 18:07
 **/
@Data
public class NewsItemEntity implements Serializable {

    @Id
    String id;

    @Indexed
    private String fromUser;

    private String fromNickName;

    private String fromUserAvatar;

    @Indexed
    private String title;
    @Field(index = false)
    private String titleImage;

    /**
     * 置顶,广告,normal
     */
    @Indexed
    private String type;

    /**
     * 未审核，拒绝，通过
     */
    @Indexed
    private String reviewType;

    @Indexed
    private Long reviewTime;

    @Indexed
    private String fromReview;

    @Indexed
    private List<FromDonateVo> fromDonate = new ArrayList<>();
    private int isLiked;
    private int isFollowed;
    private int isDonated;

    @Field(index = false)
    private String url;

    @Indexed
    private String language = "CN";

    @Indexed
    private Long time;
    private long likeNumber;
    private long commentNumber;

    @Indexed
    private Long weight;
    @Indexed
    private int index;
    @Indexed
    private int clickNum;

    @Indexed
    private String advertiser;

    @Indexed
    private Long startTime;

    @Indexed
    private Long endTime;
    @Indexed
    private List<String> tag = new ArrayList<>();
}
