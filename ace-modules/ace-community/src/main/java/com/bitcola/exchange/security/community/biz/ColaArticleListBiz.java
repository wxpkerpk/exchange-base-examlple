package com.bitcola.exchange.security.community.biz;

import com.bitcola.exchange.security.auth.client.jwt.UserAuthUtil;
import com.bitcola.exchange.security.auth.common.util.jwt.IJWTInfo;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.community.constant.ArticleConstant;
import com.bitcola.exchange.security.community.entity.*;
import com.bitcola.exchange.security.community.repostory.ArticleItemRepository;
import com.bitcola.exchange.security.community.util.AddUserInfoUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 列表
 * @author zkq
 * @create 2018-08-22 16:54
 **/
@Service
public class ColaArticleListBiz {

    /**
     * 随机的热门文章,,所以线程不安全也无所谓
     */
    private List<ArticleItemEntity> articleItemList = new ArrayList<>();

    @Autowired
    ArticleItemRepository articleItemRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    ColaFeedBiz feedBiz;

    @Autowired
    UserAuthUtil userAuthUtil;

    @Autowired
    AddUserInfoUtil addUserInfoUtil;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    /**
     * 随机获得20条热点文章
     * @param timestamp
     * @param authorization
     * @return
     */
    public List<ArticleItemEntity> list(Long timestamp, String authorization,int limit) throws Exception{
        Query query = new Query();
        query.with(new Sort(Sort.Direction.DESC, "weight"));
        query.addCriteria(Criteria.where("time").lt(timestamp)).limit(limit);
        List<ArticleItemEntity> list = mongoTemplate.find(query,ArticleItemEntity.class);
        addUserInfoUtil.articleItems(list);
        List<String> articleIds = new ArrayList<>();
        for (ArticleItemEntity item : list) {
            articleIds.add(item.getId());
            List<CommentEntity> comments = feedBiz.getComments(item.getId());
            item.setComments(comments);
        }
        if (StringUtils.isBlank(authorization)){
            return list;
        }
        IJWTInfo infoFromToken = userAuthUtil.getInfoFromToken(authorization);
        List<LikeEntity> liked = feedBiz.likedList(articleIds, infoFromToken.getId());
        for (LikeEntity likeEntity : liked) {
            String articleId = likeEntity.getParentId();
            for (ArticleItemEntity item : list) {
                if (articleId.equals(item.getId())){
                    item.setIsLiked(1);
                }
            }
        }
        return list;
    }


    /**
     * 此方法每10分钟执行一次
     *  使用 spring 定时调度
     */
//    @Scheduled(cron = "0 0/10  * * * ?")
    public void getHotArticle(){
        Query query = new Query();
        query.with(new Sort(Sort.Direction.DESC, "weight"));
        if (articleItemList.size() >= 2000){
            query.limit(2000);
        } else {
            query.limit(articleItemList.size() + 1);
        }
        articleItemList = mongoTemplate.find(query,ArticleItemEntity.class);
    }


    /**
     * 获得文章详情
     * @param id
     * @param type
     * @return
     */
    public Map<String,Object> detail(String id, String type,String token) throws Exception {
        boolean exists = false;
        if (StringUtils.isNotBlank(token)){
            IJWTInfo infoFromToken = userAuthUtil.getInfoFromToken(token);
            Query query = new Query();
            query.addCriteria(Criteria.where("parentId").is(id));
            query.addCriteria(Criteria.where("fromUser").is(infoFromToken.getId()));
            exists = mongoTemplate.exists(query, LikeEntity.class);
        }
        Map<String,Object> map = new HashMap<>();
        Object[] feedCount = feedBiz.getFeedCount(id);
        if (ArticleConstant.TYPE_ARTICLE.equals(type)){
            ArticleEntity entity = mongoTemplate.findById(id,ArticleEntity.class);
            entity.setLikeNumber((Long)feedCount[0]);
            entity.setCommentNumber((Long)feedCount[1]);
            entity.setDonateNumber((BigDecimal)feedCount[2]);
            addUserInfoUtil.articleDetail(entity);
            if (exists){
                entity.setIsLiked(1);
            }
            map.put("entity",entity);
            map.put("type",ArticleConstant.TYPE_ARTICLE);
        } else if (ArticleConstant.TYPE_SHORT_ARTICLE.equals(type)){
            ShortArticleEntity entity = mongoTemplate.findById(id,ShortArticleEntity.class);
            entity.setLikeNumber((Long)feedCount[0]);
            entity.setCommentNumber((Long)feedCount[1]);
            entity.setDonateNumber((BigDecimal)feedCount[2]);
            addUserInfoUtil.shortArticleDetail(entity);
            if (exists){
                entity.setIsLiked(1);
            }
            map.put("entity",entity);
            map.put("type",ArticleConstant.TYPE_SHORT_ARTICLE);
        }
        redisTemplate.delete(id);
        return map;
    }

    public List<ArticleItemEntity> getPostsByUserId(String userId, Long timestamp, Integer size,String authorization) throws Exception {
        Query query = new Query().addCriteria(Criteria.where("fromUser").is(userId));
        query.with(new Sort(Sort.Direction.DESC,"time")).addCriteria(Criteria.where("time").lt(timestamp)).limit(size);
        List<ArticleItemEntity> list = mongoTemplate.find(query, ArticleItemEntity.class);
        addUserInfoUtil.articleItems(list);
        if (StringUtils.isBlank(authorization)){
            return list;
        }
        IJWTInfo infoFromToken = userAuthUtil.getInfoFromToken(authorization);
        List<String> articleIds = new ArrayList<>();
        for (ArticleItemEntity item : list) {
            articleIds.add(item.getId());
        }
        List<LikeEntity> liked = feedBiz.likedList(articleIds, infoFromToken.getId());
        for (LikeEntity likeEntity : liked) {
            String articleId = likeEntity.getParentId();
            for (ArticleItemEntity item : list) {
                if (articleId.equals(item.getId())){
                    item.setIsLiked(1);
                }
            }
        }
        return list;
    }


    /**
     * 关联查询点赞的文章
     * @param userId
     * @param timestamp
     * @param authorization
     * @return
     * @throws Exception
     */
    public List<ArticleItemEntity> getLikeArticleList(String userId, Long timestamp,Integer size, String authorization) throws  Exception{
        // 区分点赞文章和评论
        LookupOperation lookupOperation = LookupOperation.newLookup().
                from("likeEntity").//关联表名
                localField("_id").//关联字段
                foreignField("parentId").//主表关联字段对应的次表字段
                as("like");
        Criteria like = Criteria.where("like.fromUser").is(userId);
        Criteria time = Criteria.where("time").lt(timestamp);
        Aggregation aggregation = Aggregation.newAggregation(lookupOperation,Aggregation.match(like),Aggregation.match(time),Aggregation.limit(size));
        List<ArticleItemEntity> items = mongoTemplate.aggregate(aggregation, "articleItemEntity", ArticleItemEntity.class).getMappedResults();
        addUserInfoUtil.articleItems(items);
        if (StringUtils.isBlank(authorization)){
            return items;
        }
        IJWTInfo infoFromToken = userAuthUtil.getInfoFromToken(authorization);
        List<String> articleIds = new ArrayList<>();
        for (ArticleItemEntity item : items) {
            articleIds.add(item.getId());
        }
        List<LikeEntity> liked = feedBiz.likedList(articleIds, infoFromToken.getId());
        for (LikeEntity likeEntity : liked) {
            String articleId = likeEntity.getParentId();
            for (ArticleItemEntity item : items) {
                if (articleId.equals(item.getId())){
                    item.setIsLiked(1);
                }
            }
        }
        return items;
    }

    public ArticleItemEntity item(String id) {
        ArticleItemEntity item = mongoTemplate.findById(id,ArticleItemEntity.class);
        Query query = new Query();
        query.addCriteria(Criteria.where("parentId").is(id));
        query.addCriteria(Criteria.where("fromUser").is(BaseContextHandler.getUserID()));
        if(mongoTemplate.exists(query,LikeEntity.class)){
            item.setIsLiked(1);
        }
        List<CommentEntity> comments = feedBiz.getComments(item.getId());
        item.setComments(comments);
        return item;
    }
}
