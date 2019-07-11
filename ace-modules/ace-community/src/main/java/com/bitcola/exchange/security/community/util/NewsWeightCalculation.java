package com.bitcola.exchange.security.community.util;

import com.bitcola.exchange.security.community.entity.NewsItemEntity;

/**
 * @author lky
 * @create 2019-04-24 10:15
 **/
public class NewsWeightCalculation {
    private static long BASE = 360000L;

    /**
     * 时间权重
     *
     * @param entity
     */
    public static NewsItemEntity setWeightWithTime(NewsItemEntity entity) {
        entity.setWeight(entity.getTime());
        return entity;
    }

    /**
     * 点赞或者评论加 1 点权重
     *
     * @param entity
     */
    public static NewsItemEntity setWeightWithLikeOrComment(NewsItemEntity entity) {
        entity.setWeight(entity.getWeight() + BASE);
        return entity;
    }

    /**
     * 点击加0.1权重
     *
     * @param entity
     */
    public static NewsItemEntity setWeightWithClick(NewsItemEntity entity) {
        entity.setWeight(entity.getWeight() + (BASE / 10));
        return entity;
    }

    /**
     * 捐赠加 5 点权重
     *
     * @param entity
     */
    public static NewsItemEntity setWeightWithDonate(NewsItemEntity entity) {
        entity.setWeight(entity.getWeight() + BASE * 5);
        return entity;
    }
}
