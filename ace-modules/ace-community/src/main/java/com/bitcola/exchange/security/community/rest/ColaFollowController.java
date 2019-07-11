package com.bitcola.exchange.security.community.rest;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.community.entity.NotificationsEntity;
import com.bitcola.community.entity.push.BasePushEntity;
import com.bitcola.community.entity.push.FeedEntity;
import com.bitcola.exchange.security.auth.client.annotation.IgnoreUserToken;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.community.biz.ColaFeedBiz;
import com.bitcola.exchange.security.community.constant.FollowConstant;
import com.bitcola.exchange.security.community.constant.PushConstant;
import com.bitcola.exchange.security.community.entity.ArticleItemEntity;
import com.bitcola.exchange.security.community.entity.FollowEntity;
import com.bitcola.exchange.security.community.entity.LikeEntity;
import com.bitcola.exchange.security.community.feign.IDataServiceFeign;
import com.bitcola.exchange.security.community.feign.IPushFeign;
import com.bitcola.exchange.security.community.repostory.ArticleItemRepository;
import com.bitcola.exchange.security.community.repostory.FollowRepository;
import com.bitcola.exchange.security.community.vo.UserVo;
import com.bitcola.me.entity.ColaUserEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 关注
 *
 * @author zkq
 * @create 2018-09-17 10:16
 **/
@RestController
@RequestMapping("follow")
public class ColaFollowController {

    @Autowired
    FollowRepository followRepository;

    @Autowired
    private ArticleItemRepository articleItemRepository;

    @Autowired
    MongoTemplate mongoTemplate;


    @Autowired
    ColaFeedBiz feedBiz;

    @Autowired
    IDataServiceFeign dataServiceFeign;

    @Autowired
    IPushFeign pushFeign;

    /**
     * 关注
     * @param map
     * @return
     */
    @RequestMapping(value = "follow",method = RequestMethod.POST)
    public AppResponse follow(@RequestBody Map<String,String> map){
        String userId = map.get("userId");
        List<FollowEntity> follow = followRepository.findByUserIdAndFollowUserId(BaseContextHandler.getUserID(), userId);
        if (follow.size() > 0){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.COMMUNITY_FOLLOWED));
        }
        if (userId.equals(BaseContextHandler.getUserID())){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.COMMUNITY_FOLLOWED_SELF));
        }
        FollowEntity entity = new FollowEntity();
        entity.setUserId(BaseContextHandler.getUserID());
        entity.setFollowUserId(userId);
        entity.setTime(System.currentTimeMillis());
        followRepository.insert(entity);
        NotificationsEntity notify = new NotificationsEntity();
        notify.setFromUser(BaseContextHandler.getUserID());
        notify.setId(UUID.randomUUID().toString());
        notify.setIsRead(0);
        notify.setTime(System.currentTimeMillis());
        notify.setType("follow");
        notify.setInfo("");
        notify.setToUser(userId);
        dataServiceFeign.insert(notify);
        ColaUserEntity info = dataServiceFeign.info(BaseContextHandler.getUserID());
        String title;
        String desc;
        ColaUserEntity toUserInfo = dataServiceFeign.info(userId);
        if (ColaLanguage.LANGUAGE_CN.equals(toUserInfo.getLanguage())){
            title = "你有新的粉丝";
            desc = info.getNickName()+"关注了你";
        } else {
            title = "You have gained a fan";
            desc = info.getNickName()+"followed you";
        }
        BasePushEntity pushEntity = getPushEntity(notify, PushConstant.FOLLOWED, desc, title, info);
        pushFeign.one(JSONObject.toJSONString(pushEntity),userId);
        return AppResponse.ok();
    }

    /**
     * 取消关注
     * @param map
     * @return
     */
    @RequestMapping(value = "cancelFollow",method = RequestMethod.POST)
    public AppResponse cancelFollow(@RequestBody Map<String,String> map){
        String userId = map.get("userId");
        List<FollowEntity> follow = followRepository.findByUserIdAndFollowUserId(BaseContextHandler.getUserID(), userId);
        if (follow.size() < 1){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.COMMUNITY_NOT_FOLLOWED));
        }
        mongoTemplate.remove(follow.get(0));
        return AppResponse.ok();
    }


    /**
     * 关注列表
     * @param page
     * @param size
     * @return
     */
    @IgnoreUserToken
    @RequestMapping(value = "followList",method = RequestMethod.GET)
    public AppResponse followList(int page,int size,String userId,String type){
        if (StringUtils.isAnyBlank(userId,type)){
            return AppResponse.paramsError();
        }
        Example<FollowEntity> example;
        if (FollowConstant.following.equalsIgnoreCase(type)){
            FollowEntity entity = new FollowEntity();
            entity.setUserId(userId);
            example = Example.of(entity,ExampleMatcher.matching().withMatcher("userId",ExampleMatcher.GenericPropertyMatchers.caseSensitive()).withIgnorePaths("followUserId","time"));
        } else if (FollowConstant.followed.equalsIgnoreCase(type)){
            FollowEntity entity = new FollowEntity();
            entity.setFollowUserId(userId);
            example = Example.of(entity,ExampleMatcher.matching().withMatcher("followUserId",ExampleMatcher.GenericPropertyMatchers.caseSensitive()).withIgnorePaths("userId","time"));
        } else {
            return AppResponse.paramsError();
        }

        Page<FollowEntity> all = followRepository.findAll(example, PageRequest.of(page, size));
        ArrayList<String> userIds = new ArrayList<>();
        for (FollowEntity entity : all) {
            if (FollowConstant.following.equalsIgnoreCase(type)){
                userIds.add(entity.getFollowUserId());
            } else {
                userIds.add(entity.getUserId());

            }
        }
        List<UserVo> users = new ArrayList<>();
        if (userIds.size()>0){
            List<ColaUserEntity> colaUserEntities = dataServiceFeign.infoByIds(userIds);
            for (ColaUserEntity colaUserEntity : colaUserEntities) {
                UserVo vo = new UserVo();
                BeanUtils.copyProperties(colaUserEntity,vo);
                users.add(vo);
            }
        }
        return AppResponse.ok().data(users);
    }


    /**
     * 关注人的动态
     *     所有关注人时间倒叙
     * @return
     */
    @RequestMapping("followArticleList")
    public AppResponse followArticleList(Integer page, Integer size){
        if (page == null) page = 0;
        if (size == null) size = 10;
        List<FollowEntity> all = mongoTemplate.find(new Query().addCriteria(Criteria.where("followUserId").is(BaseContextHandler.getUserID())), FollowEntity.class);
        List<String> follows = new ArrayList<>();
        follows.add(BaseContextHandler.getUserID());
        for (FollowEntity followEntity : all) {
            String followUserId = followEntity.getFollowUserId();
            follows.add(followUserId);
        }
        Sort sort = new Sort(Sort.Direction.DESC,"time");
        List<ArticleItemEntity> list = articleItemRepository.findByFromUserIn(follows,PageRequest.of(page, size, sort));
        List<String> articleIds = new ArrayList<>();
        for (ArticleItemEntity item : list) {
            articleIds.add(item.getId());
        }
        List<LikeEntity> liked = feedBiz.likedList(articleIds, BaseContextHandler.getUserID());
        for (LikeEntity likeEntity : liked) {
            String articleId = likeEntity.getParentId();
            for (ArticleItemEntity item : list) {
                if (articleId.equals(item.getId())){
                    item.setIsLiked(1);
                }
            }
        }
        return AppResponse.ok().data(list);
    }

    private BasePushEntity getPushEntity(NotificationsEntity entity, Integer type, String desc, String title, ColaUserEntity info){
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
        return push;
    }


}
