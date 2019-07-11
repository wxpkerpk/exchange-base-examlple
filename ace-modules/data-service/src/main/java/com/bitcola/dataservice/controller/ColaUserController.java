package com.bitcola.dataservice.controller;

import com.bitcola.community.entity.ArticleItemEntity;
import com.bitcola.community.entity.FollowEntity;
import com.bitcola.dataservice.biz.ColaUserBiz;
import com.bitcola.me.entity.ColaMeBalance;
import com.bitcola.me.entity.ColaUserEntity;
import com.bitcola.me.entity.ColaUserKyc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户信息
 *
 * @author zkq
 * @create 2018-09-03 14:11
 **/
@RestController
@RequestMapping("user")
public class ColaUserController {

    @Autowired
    ColaUserBiz biz;

    @Autowired
    MongoTemplate mongoTemplate;

    @RequestMapping(value = "getColaToken",method = RequestMethod.GET)
    public ColaMeBalance getColaToken(@RequestParam("userId") String userId){
        return biz.getColaToken(userId);
    }

    @RequestMapping(value = "infoByIds",method = RequestMethod.POST)
    public List<ColaUserEntity> infoByIds(@RequestBody ArrayList<String> userIds){
        return biz.infoByIds(userIds);
    }

    @RequestMapping(value = "info",method = RequestMethod.GET)
    public ColaUserEntity info(@RequestParam("userId") String userId){
        return biz.info(userId);
    }

    @RequestMapping(value = "infoByInviterCode",method = RequestMethod.GET)
    public ColaUserEntity infoByInviterCode(@RequestParam("inviterCode") String inviterCode){
        return biz.infoByInviterCode(inviterCode);
    }

    @RequestMapping(value = "verifyPin",method = RequestMethod.GET)
    public boolean verifyPin(@RequestParam("userId")String userID, @RequestParam("pin")String pin){
        return biz.verifyPin(userID,pin);
    }

    /**
     * 是否被关注
     * @param userId
     * @param toUserId
     * @return
     */
    @RequestMapping(value = "isFollowed",method = RequestMethod.GET)
    public int isFollowed(@RequestParam("userId")String userId,@RequestParam("toUserId")String toUserId){
        List<FollowEntity> list = mongoTemplate.find(new Query().addCriteria(Criteria.where("userId").is(userId)).addCriteria(Criteria.where("followUserId").is(toUserId)), FollowEntity.class);
        return list.size();
    }

    /**
     * 发表的文章数量
     * @param userId
     * @return
     */
    @RequestMapping(value = "getPostsByUserId",method = RequestMethod.GET)
    public long getPostsByUserId(@RequestParam("userId")String userId){
        long count = mongoTemplate.count(new Query().addCriteria(Criteria.where("fromUser").is(userId)), ArticleItemEntity.class);
        return count;
    }

    /**
     * 粉丝有多少
     * @param userId
     * @return
     */
    @RequestMapping(value = "getFollowedByUserId",method = RequestMethod.GET)
    public long getFollowedByUserId(@RequestParam("userId")String userId){
        long count = mongoTemplate.count(new Query().addCriteria(Criteria.where("followUserId").is(userId)), FollowEntity.class);
        return count;
    }

    /**
     * 他关注了多少人
     * @param userId
     * @return
     */
    @RequestMapping(value = "getFollowingByUserId",method = RequestMethod.GET)
    public long getFollowingByUserId(@RequestParam("userId")String userId){
        long count = mongoTemplate.count(new Query().addCriteria(Criteria.where("userId").is(userId)), FollowEntity.class);
        return count;
    }


    /**
     * 他发表的一篇文章
     * @param userId
     * @param timestamp
     * @return
     */
    @RequestMapping(value = "getArticleItemEntity",method = RequestMethod.GET)
    public ArticleItemEntity getArticleItemEntity(@RequestParam("userId")String userId,@RequestParam("timestamp")Long timestamp){
        Query query = new Query().addCriteria(Criteria.where("fromUser").is(userId));
        query.with(new Sort(Sort.Direction.DESC,"time")).addCriteria(Criteria.where("time").lt(timestamp)).limit(1);
        List<ArticleItemEntity> articleItemEntities = mongoTemplate.find(query, ArticleItemEntity.class);
        if (articleItemEntities.size() > 0){
            return articleItemEntities.get(0);
        }
        return null;
    }

    @RequestMapping(value = "getUserKycInfo",method = RequestMethod.GET)
    public ColaUserKyc getUserKycInfo(String userId){
        return biz.getUserKycInfo(userId);
    }


}
