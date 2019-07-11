package com.bitcola.exchange.security.community.rest;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.util.TimeUtils;
import com.bitcola.exchange.security.community.biz.ColaFeedBiz;
import com.bitcola.exchange.security.community.constant.ArticleConstant;
import com.bitcola.exchange.security.community.entity.CommentEntity;
import com.bitcola.exchange.security.community.entity.DonateEntity;
import com.bitcola.exchange.security.community.entity.LikeEntity;
import com.bitcola.exchange.security.community.feign.IDataServiceFeign;
import com.bitcola.me.entity.ColaMeBalance;
import com.bitcola.me.entity.ColaUserLimit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 反馈,评论,点赞,捐赠
 *
 * @author zkq
 * @create 2018-08-22 15:09
 **/
@RestController
@RequestMapping("feed")
public class ColaFeedController {

    @Autowired
    ColaFeedBiz biz;

    @Autowired
    ColaPublishArticleController articleController;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    IDataServiceFeign dataServiceFeign;



    @RequestMapping(value = "like",method = RequestMethod.POST)
    public AppResponse like(@RequestBody Map<String,String> params){
        String parentId = params.get("parentId");
        String type = params.get("type");
        if (StringUtils.isBlank(parentId) || StringUtils.isBlank(type)){
            return AppResponse.paramsError();
        }
        // 不能重复点赞
        Query query = new Query();
        query.addCriteria(Criteria.where("parentId").is(parentId));
        query.addCriteria(Criteria.where("fromUser").is(BaseContextHandler.getUserID()));
        if(mongoTemplate.exists(query,LikeEntity.class)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.COMMUNITY_LIKED));
        }
        biz.like(parentId,type);
        return AppResponse.ok();
    }


    @RequestMapping(value = "comment",method = RequestMethod.POST)
    public AppResponse comment(@RequestBody Map<String,String> params){
        String parentId = params.get("parentId");
        String type = params.get("type");
        String content = params.get("content");
        if (StringUtils.isBlank(parentId) || StringUtils.isBlank(type) || StringUtils.isBlank(content)){
            return AppResponse.paramsError();
        }
        long generate = articleController.generate("Publish_comment_limit" + BaseContextHandler.getUserID(), 3, TimeUnit.SECONDS);
        if (generate>1){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.COMMUNITY_PUBLISH_LIMIT));
        }
        ColaUserLimit userLimit = dataServiceFeign.getUserLimit(BaseContextHandler.getUserID(), "community");
        if (userLimit!=null){
            Long limitTime = userLimit.limitTime();
            if (limitTime>System.currentTimeMillis()){
                return AppResponse.error(ResponseCode.USER_LIMIT_CODE,
                        String.format(ColaLanguage.get(ColaLanguage.COMMUNITY_USER_LIMIT), TimeUtils.getDateFormat(limitTime)));
            }
        }
        biz.comment(parentId,type,content);
        return AppResponse.ok();
    }


    @RequestMapping(value = "donate",method = RequestMethod.POST)
    public AppResponse donate(@RequestBody Map<String,String> params){
        String parentId = params.get("parentId");
        String m = params.get("amount");
        String moneyPassword = params.get("moneyPassword");
        if (StringUtils.isBlank(parentId) || StringUtils.isBlank(m) || StringUtils.isBlank(moneyPassword)){
            return AppResponse.paramsError();
        }
        BigDecimal amount = new BigDecimal(m);
        if (amount.compareTo(BigDecimal.ZERO) < 0){
            return AppResponse.paramsError();
        }
        // 验证密码是否正确,
        boolean matches = dataServiceFeign.verifyPin(BaseContextHandler.getUserID(),moneyPassword);
        if (!matches){
            return new AppResponse(ResponseCode.PIN_ERROR_CODE,ResponseCode.PIN_ERROR_MESSAGE);
        }
        // 验证用户是否有足够的钱
        ColaMeBalance colaToken = dataServiceFeign.getColaToken(BaseContextHandler.getUserID());
        if (colaToken.getBalanceAvailable().compareTo(amount) < 0){
            return AppResponse.error(ResponseCode.NO_ENOUGH_MONEY_CODE,ResponseCode.NO_ENOUGH_MONEY_MESSAGE);
        }
        boolean d = biz.donate(parentId, amount);
        if (!d) return AppResponse.error(ResponseCode.NO_ENOUGH_MONEY_CODE,ResponseCode.NO_ENOUGH_MONEY_MESSAGE);
        return AppResponse.ok();
    }

    @RequestMapping(value = "likeList",method = RequestMethod.GET)
    public AppResponse likeList(String parentId,Long timestamp){
        List<LikeEntity> list = biz.likeList(parentId,timestamp);
        return AppResponse.ok().data(list);
    }


    @RequestMapping(value = "commentList",method = RequestMethod.GET)
    @Cached(key = "#parentId + #timestamp", cacheType = CacheType.LOCAL, expire = 2)
    public AppResponse commentList(String parentId, String timestamp,String limit, HttpServletRequest request) throws Exception{
        Long time;
        Integer limits;
        if (StringUtils.isBlank(timestamp)){
            time = System.currentTimeMillis();
        } else {
            time = Long.valueOf(timestamp);
        }
        if (StringUtils.isBlank(limit)){
            limits = 15;
        } else {
            limits = Integer.valueOf(limit);
        }
        String authorization = request.getHeader("Authorization");
        List<CommentEntity> list = biz.commentList(parentId,time,authorization,limits);
        return AppResponse.ok().data(list);
    }


    @RequestMapping(value = "donateList",method = RequestMethod.GET)
    public AppResponse donateList(String parentId,Long timestamp){
        List<DonateEntity> list = biz.donateList(parentId,timestamp);
        return AppResponse.ok().data(list);
    }

    /**
     * 删除,级连删除
     * @param type
     * @return
     */
    @RequestMapping(value = "deleteItem",method = RequestMethod.GET)
    public AppResponse deleteItem(String type,String id,Boolean force){
        if (force == null) force = false;
        if(StringUtils.isAnyBlank(type,id)){
            return AppResponse.paramsError();
        }
        switch (type){
            case ArticleConstant.TYPE_COMMENT: biz.deleteComment(id,force); break;
            case ArticleConstant.TYPE_SHORT_ARTICLE: biz.deleteShortArticle(id,force); break;
            case ArticleConstant.TYPE_ARTICLE: biz.deleteArticle(id,force); break;
            default: return AppResponse.paramsError();
        }
        return AppResponse.ok();
    }

    @RequestMapping("commentItem")
    public AppResponse commentItem(String id){
        CommentEntity entity;
        try {
            entity = biz.commentItem(id);
        } catch (Exception e) {
            return AppResponse.error(1001,"Deleted");
        }
        return AppResponse.ok().data(entity);
    }



}
