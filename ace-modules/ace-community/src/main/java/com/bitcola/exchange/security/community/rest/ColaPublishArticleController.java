package com.bitcola.exchange.security.community.rest;

import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.util.TimeUtils;
import com.bitcola.exchange.security.community.biz.ColaPublishArticleBiz;
import com.bitcola.exchange.security.community.entity.ArticleEntity;
import com.bitcola.exchange.security.community.entity.ShortArticleEntity;
import com.bitcola.exchange.security.community.feign.IDataServiceFeign;
import com.bitcola.me.entity.ColaUserLimit;
import com.netflix.discovery.converters.Auto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 发表文章
 *
 * @author zkq
 * @create 2018-08-22 15:09
 **/
@RestController
@RequestMapping("publishArticle")
public class ColaPublishArticleController {


    @Autowired
    ColaPublishArticleBiz publishArticleBiz;

    @Autowired
    IDataServiceFeign dataServiceFeign;

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 发表长文
     * @return
     */
    @RequestMapping(value = "article",method = RequestMethod.POST)
    public AppResponse article(@RequestBody ArticleEntity articleEntity){
        if (StringUtils.isBlank(articleEntity.getTitle()) || articleEntity.getTitleImage() == null || articleEntity.getContent()== null){
            return AppResponse.paramsError();
        }
        long generate = this.generate("Publish_article_limit" + BaseContextHandler.getUserID(), 60, TimeUnit.SECONDS);
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
        publishArticleBiz.publishArticle(articleEntity);
        return AppResponse.ok();
    }


    /**
     * 发表短文
     * @return
     */
    @RequestMapping(value = "shortArticle",method = RequestMethod.POST)
    public AppResponse shortArticle(@RequestBody ShortArticleEntity shortArticleEntity){
        if (shortArticleEntity.getImages().size()>9 || StringUtils.isBlank(shortArticleEntity.getContent())){
            return AppResponse.paramsError();
        }
        long generate = this.generate("Publish_article_limit" + BaseContextHandler.getUserID(), 10, TimeUnit.SECONDS);
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
        publishArticleBiz.publishShortArticle(shortArticleEntity);
        return AppResponse.ok();
    }


    public long generate(String key, long expireTime, TimeUnit unit) {
        RedisAtomicLong counter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        counter.expire(expireTime,unit);
        return counter.incrementAndGet();
    }



}
