package com.bitcola.exchange.security.community.util;

import com.bitcola.exchange.security.community.entity.ArticleItemEntity;

/**
 * 文章权重计算  文章权重设置,以文章权重来排序
 *
 * @author zkq
 * @create 2018-08-22 23:51
 **/
public class ArticleWeightCalculation {

     private static long BASE = 360000L;


    /**
     * 时间权重
     * @param entity
     */
    public static ArticleItemEntity setWeightWithTime(ArticleItemEntity entity){
        entity.setWeight(entity.getTime());
        return entity;
    }

    /**
     * 点赞或者评论加 1 点权重
     * @param entity
     */
    public static ArticleItemEntity setWeightWithLikeOrComment(ArticleItemEntity entity){
        entity.setWeight(entity.getWeight() + BASE);
        return entity;
    }

    /**
     * 捐赠加 5 点权重
     * @param entity
     */
    public static ArticleItemEntity setWeightWithDonate(ArticleItemEntity entity){
        entity.setWeight(entity.getWeight() + BASE * 5);
        return entity;
    }

}
