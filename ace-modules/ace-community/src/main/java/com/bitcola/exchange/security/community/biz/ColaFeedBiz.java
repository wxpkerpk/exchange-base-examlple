package com.bitcola.exchange.security.community.biz;

import com.alibaba.fastjson.JSONObject;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.community.entity.NotificationsEntity;
import com.bitcola.community.entity.push.BasePushEntity;
import com.bitcola.community.entity.push.FeedEntity;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.auth.client.jwt.UserAuthUtil;
import com.bitcola.exchange.security.auth.common.util.jwt.IJWTInfo;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.community.constant.ArticleConstant;
import com.bitcola.exchange.security.community.constant.PushConstant;
import com.bitcola.exchange.security.community.entity.*;
import com.bitcola.exchange.security.community.feign.IDataServiceFeign;
import com.bitcola.exchange.security.community.feign.IPushFeign;
import com.bitcola.exchange.security.community.repostory.CommentRepository;
import com.bitcola.exchange.security.community.repostory.DonateRepository;
import com.bitcola.exchange.security.community.repostory.LikeRepository;
import com.bitcola.exchange.security.community.util.AddUserInfoUtil;
import com.bitcola.exchange.security.community.util.ArticleWeightCalculation;
import com.bitcola.me.entity.ColaUserEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 评论,点赞,捐赠
 *
 * @author zkq
 * @create 2018-08-23 13:56
 **/
@Service
public class ColaFeedBiz {

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    DonateRepository donateRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    IDataServiceFeign dataServiceFeign;

    @Autowired
    UserAuthUtil userAuthUtil;

    @Autowired
    AddUserInfoUtil addUserInfoUtil;

    @Autowired
    IPushFeign pushFeign;

    @Autowired
    RedisTemplate<String, String> redisTemplate;



    /**
     * 获得点赞数,评论数,捐赠钱是多少
     * @param parentId 上级,可能是文章,也可能是评论
     * @return
     */
    public Object[] getFeedCount(String parentId){
        Query query = new Query();
        query.addCriteria(Criteria.where("parentId").is(parentId));
        long likeNumber = mongoTemplate.count(query,LikeEntity.class);
        long commentNumber = mongoTemplate.count(query,CommentEntity.class);
        BigDecimal donateNumber = BigDecimal.ZERO;
        List<DonateEntity> donateEntities = mongoTemplate.find(query, DonateEntity.class);
        for (DonateEntity donateEntity : donateEntities) {
            donateNumber = donateNumber.add(donateEntity.getAmount());
        }
        Object[] feedCount = {likeNumber,commentNumber,donateNumber};
        return feedCount;
    }


    /**
     * 点赞
     * @param parentId
     * @param type
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void like(String parentId, String type) {
        LikeEntity entity = new LikeEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setParentId(parentId);
        entity.setFromUser(BaseContextHandler.getUserID());
        entity.setFromUsername(BaseContextHandler.getUsername());
        entity.setTime(System.currentTimeMillis());
        ColaUserEntity info = dataServiceFeign.info(BaseContextHandler.getUserID());
        entity.setFromUserAvatar(info.getAvatar());
        entity.setFromUserSign(info.getSign());
        entity.setFromNickName(info.getNickName());
        likeRepository.insert(entity);
        String author;
        String actionInfo;
        String desc;
        NotificationsEntity notify = new NotificationsEntity();
        if (ArticleConstant.TYPE_COMMENT.equals(type)){
            CommentEntity c = mongoTemplate.findById(parentId, CommentEntity.class);
            c.setLikeNumber(c.getLikeNumber()+1);
            mongoTemplate.save(c);
            author = c.getFromUser();
            actionInfo = getActionInfoByComment(c.getContent());
            desc = actionInfo;
            notify.setActionType(ArticleConstant.TYPE_COMMENT);
        } else {
            ArticleItemEntity a = mongoTemplate.findById(parentId, ArticleItemEntity.class);
            a.setLikeNumber(a.getLikeNumber()+1);
            ArticleWeightCalculation.setWeightWithLikeOrComment(a);
            mongoTemplate.save(a);
            author = a.getFromUser();
            actionInfo = getActionInfoByArticle(a);
            notify.setActionType(a.getType());
            desc = getActionInfoByComment(a.getContent());
        }
        //通知被点赞的人
        if (author.equals(BaseContextHandler.getUserID())){
            return;
        }
        notify.setFromUser(BaseContextHandler.getUserID());
        notify.setActionId(parentId);
        notify.setId(UUID.randomUUID().toString());
        notify.setIsRead(0);
        notify.setTime(System.currentTimeMillis());
        notify.setType("like");
        notify.setInfo("");
        notify.setToUser(author);
        notify.setActionInfo(actionInfo);
        dataServiceFeign.insert(notify);
        Integer pushType;
        if (notify.getActionType().equals(ArticleConstant.TYPE_ARTICLE)){
            pushType = PushConstant.LIKE_ARTICLE;
        } else if (notify.getActionType().equals(ArticleConstant.TYPE_SHORT_ARTICLE)){
            pushType = PushConstant.LIKE_SHORTARTICLE;
        } else {
            pushType = PushConstant.LIKE_COMMENT;
        }
        String title;
        ColaUserEntity authInfo = dataServiceFeign.info(author);
        if (ColaLanguage.LANGUAGE_CN.equals(authInfo.getLanguage())){
            title = "你收到了新的点赞";
            desc = info.getNickName()+" 赞了你的"+get18nActionType(type,authInfo.getLanguage())+" "+desc;
        } else {
            title = "You receive a favour";
            desc = info.getNickName()+" favour your "+get18nActionType(type,authInfo.getLanguage())+" "+desc;
        }
        push(notify, pushType, desc, title, info, author);

    }

    /**
     * 评论
     * @param parentId
     * @param type
     * @param content
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void comment(String parentId, String type, String content) {
        //content = dataServiceFeign.replace(content);
        CommentEntity entity = new CommentEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setParentId(parentId);
        entity.setFromUser(BaseContextHandler.getUserID());
        entity.setFromUsername(BaseContextHandler.getUsername());
        entity.setTime(System.currentTimeMillis());
        entity.setContent(content);
        ColaUserEntity info = dataServiceFeign.info(BaseContextHandler.getUserID());
        entity.setFromUserAvatar(info.getAvatar());
        entity.setFromUserSign(info.getSign());
        entity.setFromNickName(info.getNickName());
        mongoTemplate.insert(entity);
        String author;
        String actionInfo;
        NotificationsEntity notify = new NotificationsEntity();
        if (ArticleConstant.TYPE_COMMENT.equals(type)){
            CommentEntity c = mongoTemplate.findById(parentId, CommentEntity.class);
            c.setCommentNumber(c.getCommentNumber()+1);
            mongoTemplate.save(c);
            author = c.getFromUser();
            actionInfo = getActionInfoByComment(c.getContent());
            notify.setActionType(ArticleConstant.TYPE_COMMENT);
        } else {
            ArticleItemEntity a = mongoTemplate.findById(parentId, ArticleItemEntity.class);
            a.setCommentNumber(a.getCommentNumber()+1);
            ArticleWeightCalculation.setWeightWithLikeOrComment(a);
            mongoTemplate.save(a);
            author = a.getFromUser();
            actionInfo = getActionInfoByArticle(a);
            notify.setActionType(a.getType());
        }
        if (author.equals(BaseContextHandler.getUserID())){
            return;
        }
        notify.setFromUser(BaseContextHandler.getUserID());
        notify.setActionId(parentId);
        notify.setId(UUID.randomUUID().toString());
        notify.setIsRead(0);
        notify.setTime(System.currentTimeMillis());
        notify.setType("comment");
        notify.setInfo(entity.getContent().length()>50?entity.getContent().substring(0,50)+"...":entity.getContent());
        notify.setToUser(author);
        notify.setActionInfo(actionInfo);
        dataServiceFeign.insert(notify);
        Integer pushType;
        if (notify.getActionType().equals(ArticleConstant.TYPE_ARTICLE)){
            pushType = PushConstant.COMMENT_ARTICLE;
        } else if (notify.getActionType().equals(ArticleConstant.TYPE_SHORT_ARTICLE)){
            pushType = PushConstant.COMMENT_SHORTARTICLE;
        } else {
            pushType = PushConstant.COMMENT_COMMENT;
        }
        String title;
        ColaUserEntity authInfo = dataServiceFeign.info(author);
        if (ColaLanguage.LANGUAGE_CN.equals(authInfo.getLanguage())){
            title = "你收到了新的评论";
            content = info.getNickName()+" 评论了您的"+get18nActionType(type,authInfo.getLanguage())+" "+content;
        } else {
            title = "You receive a comment!";
            content = info.getNickName()+" comment your "+get18nActionType(type,authInfo.getLanguage())+" "+content;
        }
        push(notify, pushType, content, title, info,author);

    }

    /**
     * 捐赠
     * @param parentId
     * @param amount
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean donate(String parentId, BigDecimal amount) {
        ArticleItemEntity a = mongoTemplate.findById(parentId, ArticleItemEntity.class);
        boolean success = dataServiceFeign.donate(BaseContextHandler.getUserID(),a.getFromUser(),amount);
        if (!success) return false;
        DonateEntity entity = new DonateEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setParentId(parentId);
        entity.setFromUser(BaseContextHandler.getUserID());
        entity.setFromUsername(BaseContextHandler.getUsername());
        entity.setTime(System.currentTimeMillis());
        entity.setAmount(amount);
        ColaUserEntity info = dataServiceFeign.info(BaseContextHandler.getUserID());
        entity.setFromUserAvatar(info.getAvatar());
        entity.setFromUserSign(info.getSign());
        entity.setFromNickName(info.getNickName());
        mongoTemplate.insert(entity);

        a.setDonateNumber(a.getDonateNumber().add(amount));
        ArticleWeightCalculation.setWeightWithDonate(a);
        mongoTemplate.save(a);

        String author = a.getFromUser();
        if (author.equals(BaseContextHandler.getUserID())){
            return true;
        }

        NotificationsEntity notify = new NotificationsEntity();
        String actionInfo = getActionInfoByArticle(a);
        notify.setActionType(a.getType());
        notify.setFromUser(BaseContextHandler.getUserID());
        notify.setActionId(parentId);
        notify.setId(UUID.randomUUID().toString());
        notify.setIsRead(0);
        notify.setTime(System.currentTimeMillis());
        notify.setType("donate");
        notify.setInfo(entity.getAmount().toPlainString());
        notify.setToUser(author);
        notify.setActionInfo(actionInfo);
        dataServiceFeign.insert(notify);
        Integer type;
        if (notify.getActionType().equals(ArticleConstant.TYPE_ARTICLE)){
            type = PushConstant.DONATE_ARTICLE;
        } else {
            type = PushConstant.DONATE_SHORTARTICLE;
        }
        String title;
        String desc;
        ColaUserEntity authInfo = dataServiceFeign.info(author);
        if (ColaLanguage.LANGUAGE_CN.equals(authInfo.getLanguage())){
            title = "你收到了一笔打赏";
            desc = info.getNickName()+" 打赏了您的"+get18nActionType(a.getType(),authInfo.getLanguage())+" "+a.getContent();
        } else {
            title = "You received a donate";
            desc = info.getNickName()+" donate your "+get18nActionType(a.getType(),authInfo.getLanguage())+" "+a.getContent();
        }
        push(notify, type, desc, title, info, author);
        return true;
    }


    /**
     * 点赞列表
     * @param parentId
     * @param timestamp
     * @return
     */
    public List<LikeEntity> likeList(String parentId, Long timestamp) {
        Query query = new Query();
        query.with(new Sort(Sort.Direction.ASC, "time"));
        query.addCriteria(Criteria.where("parentId").is(parentId));
        query.limit(15);
        if (timestamp != null && timestamp != 0){
            query.addCriteria(Criteria.where("time").gt(timestamp));
        }
        List<LikeEntity> likeEntities = mongoTemplate.find(query, LikeEntity.class);
        addUserInfoUtil.likeList(likeEntities);
        return likeEntities;
    }


    /**
     * 评论列表
     * @param parentId
     * @param timestamp
     * @param authorization
     * @return
     */
    public List<CommentEntity> commentList(String parentId, Long timestamp, String authorization,Integer limit) throws Exception{
        Query query = new Query();
        query.with(new Sort(Sort.Direction.DESC, "time"));
        query.addCriteria(Criteria.where("parentId").is(parentId));
        query.limit(limit);
        if (timestamp != null && timestamp != 0){
            query.addCriteria(Criteria.where("time").lt(timestamp));
        }
        List<CommentEntity> commentEntityList = mongoTemplate.find(query, CommentEntity.class);
        addUserInfoUtil.commentList(commentEntityList);
        List<String> parentIds = new ArrayList<>();
        for (CommentEntity commentEntity : commentEntityList) {
            parentIds.add(commentEntity.getId());
            List<CommentEntity> comments = this.getComments(commentEntity.getId());
            commentEntity.setComments(comments);
        }
        if (StringUtils.isBlank(authorization)){
            return commentEntityList;
        }
        IJWTInfo infoFromToken = userAuthUtil.getInfoFromToken(authorization);
        List<LikeEntity> likeEntities = likedList(parentIds, infoFromToken.getId());
        for (LikeEntity likeEntity : likeEntities) {
            String commentID = likeEntity.getParentId();
            for (CommentEntity entity : commentEntityList) {
                if (entity.getId().equals(commentID)){
                    entity.setIsLiked(1);
                }
            }
        }
        return commentEntityList;
    }

    /**
     * 捐赠列表
     * @param parentId
     * @param timestamp
     * @return
     */
    public List<DonateEntity> donateList(String parentId, Long timestamp) {
        Query query = new Query();
        query.with(new Sort(Sort.Direction.ASC, "time"));
        query.addCriteria(Criteria.where("parentId").is(parentId));
        query.limit(15);
        if (timestamp != null && timestamp != 0){
            query.addCriteria(Criteria.where("time").gt(timestamp));
        }
        List<DonateEntity> donateEntityList = mongoTemplate.find(query, DonateEntity.class);
        addUserInfoUtil.donateList(donateEntityList);
        return donateEntityList;
    }


    /**
     * 看是否点过赞
     * @param parentId
     * @param userId
     * @return
     */
    public List<LikeEntity> likedList(List<String> parentId, String userId){
        List<LikeEntity> likeEntities = likeRepository.findByParentIdInAndFromUser(parentId, userId);
        return likeEntities;
    }

    public String getActionInfoByComment(String content){
        String actionInfo;
        if (content.length()>50){
            actionInfo = content.substring(0,50)+"...";
        }else {
            actionInfo = content;
        }
        return actionInfo;
    }
    public String getActionInfoByArticle(ArticleItemEntity entity){
        Map<String,Object> map = new HashMap<>();
        if (entity.getType().equals(ArticleConstant.TYPE_ARTICLE)){
            map.put("content",entity.getContent().length()>50?entity.getContent().substring(0,50)+"...":entity.getContent());
            map.put("images",entity.getImages());
        } else {
            map.put("content",entity.getContent().length()>50?entity.getContent().substring(0,50)+"...":entity.getContent());
            map.put("images",entity.getImages());
        }
        return JSONObject.toJSONString(map);
    }

    private void push(NotificationsEntity entity, Integer type, String desc, String title, ColaUserEntity info, String author){
        RedisAtomicLong counter = new RedisAtomicLong(entity.getActionId(), redisTemplate.getConnectionFactory());
        counter.expire(10, TimeUnit.SECONDS);
        long l = counter.incrementAndGet();
        if (l > 1) return;
        BasePushEntity push = new BasePushEntity();
        push.setTitle(title);
        push.setType(type);
        push.setDesc(desc);
        FeedEntity feed = new FeedEntity();
        BeanUtils.copyProperties(entity,feed);
        feed.setFromUserAvatar(info.getAvatar());
        feed.setFromUsername(info.getUsername());
        feed.setFromUserNickName(info.getNickName());
        push.setData(feed);
        pushFeign.one(JSONObject.toJSONString(push),author);
    }

    public void deleteComment(String id, Boolean force) {
        CommentEntity comment = mongoTemplate.findById(id, CommentEntity.class);
        ArticleItemEntity article = mongoTemplate.findById(comment.getParentId(), ArticleItemEntity.class);
        if (comment.getFromUser().equals(BaseContextHandler.getUserID())||force){
            // 删除自己发的
            delComment(id);
        } else {
            if (article == null){
                // 删除的是二级评论 楼主和层主可以删除
                CommentEntity subComment = mongoTemplate.findById(comment.getParentId(), CommentEntity.class);
                ArticleItemEntity topArticle = mongoTemplate.findById(subComment.getParentId(), ArticleItemEntity.class);
                if (subComment.getFromUser().equals(BaseContextHandler.getUserID())||
                        topArticle.getFromUser().equals(BaseContextHandler.getUserID())||force){
                    delComment(id);
                }
            } else if (article.getFromUser().equals(BaseContextHandler.getUserID())||force){
                // 删除自己文章下面的评论
                delComment(id);
            }
        }
        // 自己这篇文章的评论数少1
        if (article!=null){
            article.setCommentNumber(article.getCommentNumber()-1);
            mongoTemplate.save(article);
        } else {
            CommentEntity parentComment = mongoTemplate.findById(comment.getParentId(), CommentEntity.class);
            parentComment.setCommentNumber(parentComment.getCommentNumber()-1);
            mongoTemplate.save(parentComment);
        }
    }

    public void delComment(String id){
        Query query = new Query();
        query.addCriteria(Criteria.where("parentId").is(id));
        mongoTemplate.remove(query, CommentEntity.class);
        query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        mongoTemplate.remove(query,CommentEntity.class);
    }

    public void deleteShortArticle(String id, Boolean force) {
        ArticleItemEntity article = mongoTemplate.findById(id, ArticleItemEntity.class);
        if (article.getFromUser().equals(BaseContextHandler.getUserID())||force){
            Query query = new Query();
            query.addCriteria(Criteria.where("id").is(id));
            mongoTemplate.remove(query, ShortArticleEntity.class);
            mongoTemplate.remove(query, ArticleItemEntity.class);
            // 删除评论,点赞,打赏
            query = new Query();
            query.addCriteria(Criteria.where("parentId").is(id));
            mongoTemplate.remove(query, LikeEntity.class);
            mongoTemplate.remove(query, DonateEntity.class);
            List<CommentEntity> commentEntities = mongoTemplate.find(query, CommentEntity.class);
            for (CommentEntity commentEntity : commentEntities) {
                this.delComment(commentEntity.getId());
            }
        }
    }

    public void deleteArticle(String id, Boolean force) {
        ArticleItemEntity article = mongoTemplate.findById(id, ArticleItemEntity.class);
        if (article.getFromUser().equals(BaseContextHandler.getUserID())||force){
            //删除文章, item
            Query query = new Query();
            query.addCriteria(Criteria.where("id").is(id));
            mongoTemplate.remove(query, ArticleEntity.class);
            mongoTemplate.remove(query, ArticleItemEntity.class);
            // 删除评论,点赞,打赏
            query = new Query();
            query.addCriteria(Criteria.where("parentId").is(id));
            mongoTemplate.remove(query, LikeEntity.class);
            mongoTemplate.remove(query, DonateEntity.class);
            List<CommentEntity> commentEntities = mongoTemplate.find(query, CommentEntity.class);
            for (CommentEntity commentEntity : commentEntities) {
                this.delComment(commentEntity.getId());
            }
        }
    }

    public CommentEntity commentItem(String id) {
        CommentEntity comment = mongoTemplate.findById(id, CommentEntity.class);
        Query query = new Query();
        query.addCriteria(Criteria.where("parentId").is(id));
        query.addCriteria(Criteria.where("fromUser").is(BaseContextHandler.getUserID()));
        if(mongoTemplate.exists(query,LikeEntity.class)){
            comment.setIsLiked(1);
        }
        comment.setComments(this.getComments(id));
        return comment;
    }

    @Cached(key = "#parentId", cacheType = CacheType.LOCAL, expire = 10)
    public List<CommentEntity> getComments(String parentId){
        Query query = new Query();
        query.addCriteria(Criteria.where("parentId").is(parentId));
        query.with(new Sort(Sort.Direction.DESC, "time"));
        query.limit(3);
        return mongoTemplate.find(query, CommentEntity.class);
    }

    private String get18nActionType(String type,String language){
        if (ColaLanguage.LANGUAGE_CN.equalsIgnoreCase(language)){
            switch (type) {
                case ArticleConstant.TYPE_ARTICLE:return "文章";
                case ArticleConstant.TYPE_SHORT_ARTICLE:return "微文";
                case ArticleConstant.TYPE_COMMENT:return "评论";
            }
        } else {
            switch (type) {
                case ArticleConstant.TYPE_ARTICLE:return "article";
                case ArticleConstant.TYPE_SHORT_ARTICLE:return "short article";
                case ArticleConstant.TYPE_COMMENT:return "comment";
            }
        }
        return "";
    }

}
