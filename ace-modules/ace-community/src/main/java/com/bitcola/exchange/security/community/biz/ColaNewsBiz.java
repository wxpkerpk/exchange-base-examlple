package com.bitcola.exchange.security.community.biz;

import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppPageResponse;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.community.constant.NewsConstant;
import com.bitcola.exchange.security.community.entity.*;
import com.bitcola.exchange.security.community.feign.IDataServiceFeign;
import com.bitcola.exchange.security.community.feign.IExchangeFeign;
import com.bitcola.exchange.security.community.repostory.*;
import com.bitcola.exchange.security.community.util.NewsWeightCalculation;
import com.bitcola.exchange.security.community.vo.FromDonateVo;
import com.bitcola.me.entity.ColaUserEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author lky
 * @create 2019-04-23 18:45
 **/
@Service
public class ColaNewsBiz {
    @Autowired
    NewsItemRepository newsItemRepository;

    @Autowired
    NewsRepository newsRepository;

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    NewsCommentRepository commentRepository;

    @Autowired
    NewsBannerRepository bannerRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    IDataServiceFeign dataServiceFeign;

    @Autowired
    IExchangeFeign exchangeFeign;

    @Autowired
    FollowRepository followRepository;

    public void addNews(NewsEntity newsEntity) {
        String id = UUID.randomUUID().toString();
        newsEntity.setId(id);
        newsEntity.setTime(System.currentTimeMillis());
        newsEntity.setFromUser(BaseContextHandler.getUserID());
        newsEntity.setFromNickName(BaseContextHandler.getName());
        ColaUserEntity info = dataServiceFeign.info(BaseContextHandler.getUserID());
        newsEntity.setFromUserAvatar(info.getAvatar());
        newsEntity.setFromUser(info.getSysUserID());
        newsEntity.setFromNickName(info.getNickName());
        newsEntity.setLanguage(info.getLanguage());
        NewsWeightCalculation.setWeightWithTime(newsEntity);
        newsEntity.setReviewType(NewsConstant.TYPE_REVIEW_PENDING);
        newsRepository.insert(newsEntity);

        NewsItemEntity newsItemEntity = new NewsItemEntity();
        newsItemEntity.setId(id);
        newsItemEntity.setTime(System.currentTimeMillis());
        newsItemEntity.setFromUser(info.getSysUserID());
        newsItemEntity.setFromNickName(info.getNickName());
        newsItemEntity.setLanguage(info.getLanguage());
        newsItemEntity.setType(newsEntity.getType());
        NewsWeightCalculation.setWeightWithTime(newsItemEntity);
        newsItemEntity.setTitle(newsEntity.getTitle());
        newsItemEntity.setTitleImage(newsEntity.getTitleImage());
        newsItemEntity.setUrl(newsEntity.getUrl());
        newsItemEntity.setReviewType(NewsConstant.TYPE_REVIEW_PENDING);
        newsItemRepository.insert(newsItemEntity);
    }

    public AppResponse findDetailById(String id) {
        NewsEntity entity = mongoTemplate.findById(id, NewsEntity.class);
        if (entity != null) {
            ColaUserEntity info = dataServiceFeign.info(entity.getFromUser());
            entity.setFromUserAvatar(info.getAvatar());
            entity.setFromUser(info.getSysUserID());
            entity.setFromNickName(info.getNickName());
            Query query = new Query();
            query.addCriteria(Criteria.where("parentId").is(id));
            long likeNumber = mongoTemplate.count(query, LikeEntity.class);
            entity.setLikeNumber(likeNumber);

            List<FollowEntity> follow = followRepository.findByUserIdAndFollowUserId(BaseContextHandler.getUserID(), entity.getFromUser());
            if (follow.size() > 0) {
                entity.setIsFollowed(1);
            }

            //点击量
            NewsEntity newsEntity = mongoTemplate.findById(id, NewsEntity.class);
            newsEntity.setClickNum(newsEntity.getClickNum() + 1);
            NewsWeightCalculation.setWeightWithClick(newsEntity);
            mongoTemplate.save(newsEntity);

            NewsItemEntity newsItemEntity = mongoTemplate.findById(id, NewsItemEntity.class);
            newsItemEntity.setClickNum(newsItemEntity.getClickNum() + 1);
            NewsWeightCalculation.setWeightWithClick(newsItemEntity);
            mongoTemplate.save(newsItemEntity);

            return AppResponse.ok().data(entity);
        } else {
            return AppResponse.error("没有这一条");
        }
    }

    public void deletes(List<String> idList) {
        for (String id : idList) {
            Query query = new Query();
            query.addCriteria(Criteria.where("id").is(id));
            mongoTemplate.remove(query, NewsEntity.class);
            mongoTemplate.remove(query, NewsItemEntity.class);
            Query parentQuery = new Query();
            parentQuery.addCriteria(Criteria.where("parentId").is(id));
            mongoTemplate.remove(parentQuery, NewsDonateEntity.class);
            mongoTemplate.remove(parentQuery, LikeEntity.class);
            mongoTemplate.remove(parentQuery, NewsCommentEntity.class);
        }
    }

    public void reviewState(String id, String type, List<String> tagList) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        NewsEntity newsEntity = mongoTemplate.findOne(query, NewsEntity.class);
        newsEntity.setReviewType(type);
        newsEntity.setFromReview(BaseContextHandler.getUserID());
        newsEntity.setReviewTime(System.currentTimeMillis());
        newsEntity.setTag(tagList);
        mongoTemplate.save(newsEntity);

        NewsItemEntity newsItemEntity = mongoTemplate.findOne(query, NewsItemEntity.class);
        newsItemEntity.setReviewType(type);
        newsItemEntity.setFromReview(BaseContextHandler.getUserID());
        newsItemEntity.setReviewTime(System.currentTimeMillis());
        newsItemEntity.setTag(tagList);
        mongoTemplate.save(newsItemEntity);
    }

    public void changeType(String id, String type, String url, String advertiser, Long startTime, Long endTime, int index) {
        NewsEntity entity = mongoTemplate.findById(id, NewsEntity.class);
        entity.setType(type);
        entity.setUrl(url);
        entity.setAdvertiser(advertiser);
        entity.setStartTime(startTime);
        entity.setEndTime(endTime);
        entity.setIndex(index);
        NewsItemEntity itemEntity = mongoTemplate.findById(id, NewsItemEntity.class);
        itemEntity.setType(type);
        itemEntity.setUrl(url);
        itemEntity.setAdvertiser(advertiser);
        itemEntity.setStartTime(startTime);
        itemEntity.setEndTime(endTime);
        itemEntity.setIndex(index);
        mongoTemplate.save(entity);
        mongoTemplate.save(itemEntity);
    }

    public AppPageResponse list(int limit, Long timestamp) {
        Map<String, List<NewsItemEntity>> map = new HashMap<String, List<NewsItemEntity>>();
        if (timestamp == null) {
            Query topQuery = new Query();
            topQuery.with(new Sort(Sort.Direction.DESC, "weight"));
            topQuery.addCriteria(Criteria.where("type").is(NewsConstant.TYPE_TOP));
            topQuery.addCriteria(Criteria.where("reviewType").is(NewsConstant.TYPE_REVIEW_SUCCESS));
            map.put("top", mongoTemplate.find(topQuery, NewsItemEntity.class));

            Query adQuery = new Query();
            adQuery.with(new Sort(Sort.Direction.DESC, "weight"));
            adQuery.addCriteria(Criteria.where("type").is(NewsConstant.TYPE_AD));
            adQuery.addCriteria(Criteria.where("reviewType").is(NewsConstant.TYPE_REVIEW_SUCCESS));
            map.put("advertisement", mongoTemplate.find(adQuery, NewsItemEntity.class));
        }
        Query query = new Query();
        query.with(new Sort(Sort.Direction.DESC, "weight"));
        if (timestamp != null) {
            query.addCriteria(Criteria.where("weight").lt(timestamp)).limit(limit);
        }
        query.addCriteria(Criteria.where("type").is(NewsConstant.TYPE_NORMAL));
        query.addCriteria(Criteria.where("reviewType").is(NewsConstant.TYPE_REVIEW_SUCCESS));

        List<NewsItemEntity> list = mongoTemplate.find(query, NewsItemEntity.class);
        if (list.size() != 0) {
            ArrayList<String> ids = new ArrayList<>();
            for (NewsItemEntity entity : list) {
                ids.add(entity.getFromUser());
            }
            List<ColaUserEntity> colaUserEntities = dataServiceFeign.infoByIds(ids);
            for (NewsItemEntity entity : list) {
                for (ColaUserEntity user : colaUserEntities) {
                    if (user.getSysUserID().equals(entity.getFromUser())) {
                        entity.setFromNickName(user.getNickName());
                        entity.setFromUserAvatar(user.getAvatar());
                    }
                }
            }
        }
        map.put("normal", list);
        AppPageResponse appPageResponse = new AppPageResponse();
        Long weight = timestamp;
        if (list.size() > 0) {
            weight = list.get(list.size() - 1).getWeight();
        }
        appPageResponse.setCursor(weight);
        appPageResponse.setMessage(ResponseCode.SUCCESS_MESSAGE);
        appPageResponse.setStatus(ResponseCode.SUCCESS_CODE);
        appPageResponse.setData(map);
        return appPageResponse;
    }

    public Map<String, Object> reviewList(int limit, int page, Long timestamp, String id, String fromUser, String isDonated, String reviewType, String type, String sortType) {
        Map<String, Object> map = new HashMap<String, Object>();
        Query query = new Query();
        switch (sortType) {
            case NewsConstant.SORTTYPE_CLICK_UP:
                query.with(new Sort(Sort.Direction.ASC, "clickNum"));
                break;
            case NewsConstant.SORTTYPE_CLICK_DOWN:
                query.with(new Sort(Sort.Direction.DESC, "clickNum"));
                break;
            case NewsConstant.SORTTYPE_WEIGHT:
                query.with(new Sort(Sort.Direction.DESC, "weight"));
                break;
        }
        query.with(new Sort(Sort.Direction.ASC, "time"));
        query.addCriteria(Criteria.where("time").gt(timestamp));
        if (!StringUtils.isBlank(id)) {
            query.addCriteria(Criteria.where("id").is(id));
        }
        if (!StringUtils.isBlank(fromUser)) {
            query.addCriteria(Criteria.where("fromUser").is(fromUser));
        }
        if (!StringUtils.isBlank(isDonated)) {
            if (isDonated.equals("0")) {
                query.addCriteria(Criteria.where("fromDonate").size(0));
            } else {
                query.addCriteria(Criteria.where("fromDonate").not().size(0));
            }
        }

        if (!StringUtils.isBlank(reviewType)) {
            query.addCriteria(Criteria.where("reviewType").is(reviewType));
        }
        if (!StringUtils.isBlank(type)) {
            query.addCriteria(Criteria.where("type").is(type));
        }
        long total = mongoTemplate.count(query, NewsEntity.class);
        map.put("total", total);
        Pageable pageable = PageRequest.of(page - 1, limit);
        query.with(pageable);
        List<NewsEntity> list = mongoTemplate.find(query, NewsEntity.class);

        if (list.size() != 0) {
            ArrayList<String> ids = new ArrayList<>();
            for (NewsEntity entity : list) {
                ids.add(entity.getFromUser());
            }
            List<ColaUserEntity> colaUserEntities = dataServiceFeign.infoByIds(ids);
            for (NewsEntity entity : list) {
                for (ColaUserEntity user : colaUserEntities) {
                    if (user.getSysUserID().equals(entity.getFromUser())) {
                        entity.setFromNickName(user.getNickName());
                        entity.setFromUserAvatar(user.getAvatar());
                    }
                }
            }
        }
        map.put("reviewList", list);
        return map;
    }


    public List<NewsItemEntity> itemList(Long timestamp, int limit) {
        Query query = new Query();
        query.with(new Sort(Sort.Direction.DESC, "weight"));
        query.addCriteria(Criteria.where("time").lt(timestamp)).limit(limit);
        List<NewsItemEntity> list = mongoTemplate.find(query, NewsItemEntity.class);
        if (list.size() != 0) {
            ArrayList<String> ids = new ArrayList<>();
            for (NewsItemEntity entity : list) {
                ids.add(entity.getFromUser());
            }
            List<ColaUserEntity> colaUserEntities = dataServiceFeign.infoByIds(ids);
            for (NewsItemEntity entity : list) {
                for (ColaUserEntity user : colaUserEntities) {
                    if (user.getSysUserID().equals(entity.getFromUser())) {
                        entity.setFromNickName(user.getNickName());
                        entity.setFromUserAvatar(user.getAvatar());
                    }
                }
            }
        }
        return list;
    }

    public Map<String, Object> historyList(Long timestamp, int limit, String fromUser, boolean isFresh) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (isFresh) {
            Query query = new Query();
            query.with(new Sort(Sort.Direction.DESC, "weight"));
            query.addCriteria(Criteria.where("reviewType").is(NewsConstant.TYPE_REVIEW_SUCCESS));
            List<NewsEntity> allList = mongoTemplate.find(query, NewsEntity.class);

            Query donateQuery = new Query();
            donateQuery.addCriteria(Criteria.where("fromUser").is(fromUser));
            List<NewsDonateEntity> donateList = mongoTemplate.find(donateQuery, NewsDonateEntity.class);
            int likeNum = 0;
            BigDecimal donateNum = BigDecimal.ZERO;
            for (int i = 0; i < allList.size(); i++) {
                likeNum += allList.get(i).getLikeNumber();
            }
            for (int i = 0; i < donateList.size(); i++) {
                donateNum = donateNum.add(donateList.get(i).getAmount().multiply(exchangeFeign.getCoinPrice(donateList.get(i).getCoinCode()).getData()));
            }
            map.put("likeNum", likeNum);
            map.put("donateNum", donateNum);
            map.put("newsNum", allList.size());
        }
        Query listQuery = new Query();
        listQuery.with(new Sort(Sort.Direction.DESC, "weight"));
        listQuery.addCriteria(Criteria.where("time").lt(timestamp)).limit(limit);
        listQuery.addCriteria(Criteria.where("reviewType").is(NewsConstant.TYPE_REVIEW_SUCCESS));
        listQuery.addCriteria(Criteria.where("fromUser").is(fromUser));

        List<NewsEntity> list = mongoTemplate.find(listQuery, NewsEntity.class);

        if (list.size() != 0) {
            ArrayList<String> ids = new ArrayList<>();
            for (NewsEntity entity : list) {
                ids.add(entity.getFromUser());
            }
            List<ColaUserEntity> colaUserEntities = dataServiceFeign.infoByIds(ids);
            for (NewsEntity entity : list) {
                for (ColaUserEntity user : colaUserEntities) {
                    if (user.getSysUserID().equals(entity.getFromUser())) {
                        entity.setFromNickName(user.getNickName());
                        entity.setFromUserAvatar(user.getAvatar());
                    }
                }
            }
        }
        map.put("list", list);
        return map;
    }

    public void like(String parentId) {
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

        NewsEntity newsEntity = mongoTemplate.findById(parentId, NewsEntity.class);
        newsEntity.setLikeNumber(newsEntity.getLikeNumber() + 1);
        NewsWeightCalculation.setWeightWithLikeOrComment(newsEntity);
        newsEntity.setIsLiked(1);
//        newsEntity.setfrom(BaseContextHandler.getUserID());
        mongoTemplate.save(newsEntity);

        NewsItemEntity newsItemEntity = mongoTemplate.findById(parentId, NewsItemEntity.class);
        newsItemEntity.setLikeNumber(newsItemEntity.getLikeNumber() + 1);
        newsItemEntity.setIsLiked(1);
        NewsWeightCalculation.setWeightWithLikeOrComment(newsItemEntity);
//        newsItemEntity.setFromDonate(BaseContextHandler.getUserID());
        mongoTemplate.save(newsItemEntity);
    }

    public void donate(String parentId, BigDecimal amount, String coinCode) {
        NewsEntity newsEntity = mongoTemplate.findById(parentId, NewsEntity.class);
        boolean success = dataServiceFeign.donateNews(BaseContextHandler.getUserID(), newsEntity.getFromUser(), amount, coinCode);
        if (!success) return;
        NewsDonateEntity entity = new NewsDonateEntity();
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
        entity.setCoinCode(coinCode);
        mongoTemplate.insert(entity);

        NewsWeightCalculation.setWeightWithDonate(newsEntity);
        newsEntity.getFromDonate().add(new FromDonateVo(BaseContextHandler.getUserID(), amount, coinCode));
        newsEntity.setIsDonated(1);
        mongoTemplate.save(newsEntity);

        NewsItemEntity newsItemEntity = mongoTemplate.findById(parentId, NewsItemEntity.class);
        NewsWeightCalculation.setWeightWithDonate(newsItemEntity);
        newsItemEntity.getFromDonate().add(new FromDonateVo(BaseContextHandler.getUserID(), amount, coinCode));
        newsItemEntity.setIsDonated(1);
        mongoTemplate.save(newsItemEntity);

//        NotificationsEntity notify = new NotificationsEntity();
//        String actionInfo = newsEntity.getTitle();
//        notify.setActionType(newsEntity.getType());
//        notify.setFromUser(BaseContextHandler.getUserID());
//        notify.setActionId(parentId);
//        notify.setId(UUID.randomUUID().toString());
//        notify.setIsRead(0);
//        notify.setTime(System.currentTimeMillis());
//        notify.setType("donate");
//        notify.setInfo(entity.getAmount().toPlainString() + coinCode);
//        notify.setToUser(newsEntity.getFromUser());
//        notify.setActionInfo(actionInfo);
//        dataServiceFeign.insert(notify);
    }


    public void setWeight(String id, Long weight) {
        NewsEntity newsEntity = mongoTemplate.findById(id, NewsEntity.class);
        newsEntity.setWeight(newsEntity.getWeight() + weight);
        mongoTemplate.save(newsEntity);

        NewsItemEntity newsItemEntity = mongoTemplate.findById(id, NewsItemEntity.class);
        newsItemEntity.setWeight(newsItemEntity.getWeight() + weight);
        mongoTemplate.save(newsItemEntity);
    }

    public void comment(String parentId, String content) {
        NewsCommentEntity entity = new NewsCommentEntity();
        entity.setContent(content);
        String id = UUID.randomUUID().toString();
        entity.setId(id);
        entity.setParentId(parentId);
        entity.setTime(System.currentTimeMillis());
        entity.setFromUser(BaseContextHandler.getUserID());
        entity.setFromUsername(BaseContextHandler.getUsername());
        entity.setFromNickName(BaseContextHandler.getName());
        ColaUserEntity info = dataServiceFeign.info(BaseContextHandler.getUserID());
        entity.setFromUserAvatar(info.getAvatar());
        entity.setFromUser(info.getSysUserID());
        entity.setFromNickName(info.getNickName());
        entity.setFromUsername(info.getUsername());
        commentRepository.insert(entity);

        NewsEntity newsEntity = mongoTemplate.findById(parentId, NewsEntity.class);
        newsEntity.setCommentNumber(newsEntity.getCommentNumber() + 1);
        NewsWeightCalculation.setWeightWithLikeOrComment(newsEntity);
        mongoTemplate.save(newsEntity);

        NewsItemEntity newsItemEntity = mongoTemplate.findById(parentId, NewsItemEntity.class);
        newsItemEntity.setCommentNumber(newsItemEntity.getCommentNumber() + 1);
        NewsWeightCalculation.setWeightWithLikeOrComment(newsItemEntity);
        mongoTemplate.save(newsItemEntity);
    }

    public void deleteComment(String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        NewsCommentEntity entity = mongoTemplate.findOne(query, NewsCommentEntity.class);
        commentRepository.delete(entity);
    }

    public List<NewsCommentEntity> getCommentList(String parentId, int limit, Long timestamp) {
        Query query = new Query();
        query.with(new Sort(Sort.Direction.DESC, "time"));
        query.addCriteria(Criteria.where("time").lt(timestamp)).limit(limit);
        query.addCriteria(Criteria.where("parentId").is(parentId));
        List<NewsCommentEntity> list = mongoTemplate.find(query, NewsCommentEntity.class);
        if (list.size() != 0) {
            ArrayList<String> ids = new ArrayList<>();
            for (NewsCommentEntity entity : list) {
                ids.add(entity.getFromUser());
            }
            List<ColaUserEntity> colaUserEntities = dataServiceFeign.infoByIds(ids);
            for (NewsCommentEntity entity : list) {
                for (ColaUserEntity user : colaUserEntities) {
                    if (user.getSysUserID().equals(entity.getFromUser())) {
                        entity.setFromNickName(user.getNickName());
                        entity.setFromUserAvatar(user.getAvatar());
                    }
                }
            }
        }
        return list;
    }


    public void publishBanner(String index, String image, String url) {
        NewsBannerEntity entity = new NewsBannerEntity();
        String id = UUID.randomUUID().toString();
        entity.setId(id);
        entity.setIndex(index);
        entity.setImage(image);
        entity.setUrl(url);
        bannerRepository.insert(entity);
    }

    public void deleteBanner(String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        NewsBannerEntity entity = mongoTemplate.findOne(query, NewsBannerEntity.class);
        bannerRepository.delete(entity);
    }

    public void changeBanner(NewsBannerEntity entity) {
        bannerRepository.save(entity);
    }

    public List<NewsBannerEntity> getBannerList() {
        Query query = new Query();
        query.with(new Sort(Sort.Direction.ASC, "index"));
        List<NewsBannerEntity> list = mongoTemplate.find(query, NewsBannerEntity.class);
        return list;
    }

}
