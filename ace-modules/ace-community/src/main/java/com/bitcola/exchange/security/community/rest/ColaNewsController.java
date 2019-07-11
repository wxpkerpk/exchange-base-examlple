package com.bitcola.exchange.security.community.rest;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.exchange.security.auth.client.annotation.IgnoreUserToken;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppPageResponse;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.community.biz.ColaNewsBiz;
import com.bitcola.exchange.security.community.constant.NewsConstant;
import com.bitcola.exchange.security.community.entity.*;
import com.bitcola.exchange.security.community.feign.IDataServiceFeign;
import com.bitcola.exchange.security.community.feign.IExchangeFeign;
import com.bitcola.exchange.security.community.vo.ReviewStateVo;
import com.bitcola.exchange.security.community.xss.HTMLFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author lky
 * @create 2019-04-23 18:15
 **/
@RestController
@RequestMapping("/news")
public class ColaNewsController {
    @Autowired
    ColaNewsBiz colaNewsBiz;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    IExchangeFeign exchangeFeign;

    @Autowired
    IDataServiceFeign dataServiceFeign;

    @Autowired
    HTMLFilter htmlFilter;
    /**
     * type填advertisement时,url不填是跳进页面,填了跳网页
     *
     * @param newsEntity
     * @return
     */
    @RequestMapping(value = "publish", method = RequestMethod.POST)
    public AppResponse publish(@RequestBody NewsEntity newsEntity) {
        if (StringUtils.isBlank(newsEntity.getType()) || StringUtils.isBlank(newsEntity.getTitle()) || StringUtils.isBlank(newsEntity.getTitleImage())) {
            return AppResponse.paramsError();
        }

        if (newsEntity.getContent().length() > 1 * 1024 * 1024 || newsEntity.getTitle().length() > 32 || newsEntity.getIntroduction().length() > 150) {
            return AppResponse.error(ColaLanguage.get(ColaLanguage.STRING_LIMIT));
        }

//        if (dataServiceFeign.contain(newsEntity.getContent()) || dataServiceFeign.contain(newsEntity.getIntroduction())|| dataServiceFeign.contain(newsEntity.getTitle()) ){
//            return AppResponse.ok().data(false);
//        }
        newsEntity.setContent(dataServiceFeign.replace(htmlFilter.filter(newsEntity.getContent())));
        newsEntity.setIntroduction(dataServiceFeign.replace(newsEntity.getIntroduction()));
        colaNewsBiz.addNews(newsEntity);
        return AppResponse.ok();
    }

    /**
     * @param id 资讯id
     * @return
     */
    @RequestMapping(value = "details", method = RequestMethod.GET)
    @IgnoreUserToken
    public AppResponse details(@RequestParam(value = "id") String id) {
        return colaNewsBiz.findDetailById(id);
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public AppResponse deletes(@RequestBody List<String> idList) {

        colaNewsBiz.deletes(idList);
        return AppResponse.ok();
    }

    @RequestMapping(value = "reviewState", method = RequestMethod.POST)
    public AppResponse reviewState(@RequestBody ReviewStateVo params) {
        if (StringUtils.isBlank(params.getId()) || StringUtils.isBlank(params.getReviewType())) {
            return AppResponse.paramsError();
        }

        colaNewsBiz.reviewState(params.getId(), params.getReviewType(),params.getTag());
        return AppResponse.ok();
    }

    @RequestMapping(value = "changeType", method = RequestMethod.POST)
    public AppResponse changeType(@RequestBody Map<String, String> params) {
        String id = params.get("id");
        String type = params.get("type");
        String url = "";
        if (StringUtils.isBlank(id) || StringUtils.isBlank(type)) {
            return AppResponse.paramsError();
        }

        if (params.containsKey("url")) {
            url = params.get("url");
        }
        String advertiser = "";
        Long startTime = 0L;
        Long endTime = 0L;
        int index = 0;
        if (params.containsKey("advertiser")) {
            advertiser = params.get("advertiser");
        }
        if (params.containsKey("startTime")) {
            startTime = Long.valueOf(params.get("startTime"));
        }
        if (params.containsKey("endTime")) {
            endTime = Long.valueOf(params.get("endTime"));
        }

        if (params.containsKey("index")) {
            index = Integer.parseInt(params.get("index"));
        }
        colaNewsBiz.changeType(id, type, url, advertiser, startTime, endTime,index);
        return AppResponse.ok();
    }


    @RequestMapping(value = "list", method = RequestMethod.GET)
    public AppPageResponse list(Integer limit, Long timestamp) {
        if (limit == null) {
            limit = 15;
        } else {
            limit = Integer.valueOf(limit);
        }
        return colaNewsBiz.list( limit, timestamp);
    }

    @RequestMapping(value = "reviewList", method = RequestMethod.GET)
    public AppResponse reviewList(String limit, String page, String timestamp, String id, String fromUser, String isDonated, String reviewType, String type, String sortType) {
        Long time;
        Integer limits;
        Integer pages;
        String sort = NewsConstant.SORTTYPE_TIME;
        if (StringUtils.isBlank(timestamp) || timestamp.equals("0")) {
            time = 0L;
        } else {
            time = Long.valueOf(timestamp);
        }

        if (StringUtils.isBlank(limit)) {
            limits = 15;
        } else {
            limits = Integer.valueOf(limit);
        }

        if (StringUtils.isBlank(page)) {
            pages = 1;
        } else {
            pages = Integer.valueOf(page);
        }
        if (!StringUtils.isBlank(sortType)) {
            sort = sortType;
        }
        return AppResponse.ok().data(colaNewsBiz.reviewList(limits, pages, time, id, fromUser, isDonated, reviewType, type, sort));
    }


    @RequestMapping(value = "like", method = RequestMethod.POST)
    public AppResponse like(@RequestBody Map<String, String> params) {
        String parentId = params.get("parentId");
        if (StringUtils.isBlank(parentId)) {
            return AppResponse.paramsError();
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("parentId").is(parentId));
        query.addCriteria(Criteria.where("fromUser").is(BaseContextHandler.getUserID()));
        if (mongoTemplate.exists(query, LikeEntity.class)) {
            return AppResponse.error(ColaLanguage.get(ColaLanguage.COMMUNITY_LIKED));
        }
        colaNewsBiz.like(parentId);
        return AppResponse.ok();
    }

    @RequestMapping(value = "donate", method = RequestMethod.POST)
    public AppResponse donate(@RequestBody Map<String, String> params) {
        String parentId = params.get("parentId");
        String m = params.get("amount");
        String coinCode = params.get("coinCode");
        String moneyPassword = params.get("moneyPassword");
        if (StringUtils.isBlank(parentId) || StringUtils.isBlank(m) || StringUtils.isBlank(moneyPassword)) {
            return AppResponse.paramsError();
        }
        BigDecimal amount = new BigDecimal(m);
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return AppResponse.paramsError();
        }
        // 验证密码是否正确,
        boolean matches = dataServiceFeign.verifyPin(BaseContextHandler.getUserID(), moneyPassword);
        if (!matches) {
            return new AppResponse(ResponseCode.PIN_ERROR_CODE, ResponseCode.PIN_ERROR_MESSAGE);
        }
        // 验证用户是否有足够的钱
        String balance = JSONObject.toJSONString(exchangeFeign.getBalance(coinCode).getData());
        BigDecimal balanceAvailable = JSONObject.parseArray(balance).getJSONObject(0).getBigDecimal("balanceAvailable");
//        ColaMeBalance colaToken = userFeign.getColaToken(BaseContextHandler.getUserID());
        if (balanceAvailable.compareTo(amount) < 0) {
            return AppResponse.error(ResponseCode.NO_ENOUGH_MONEY_CODE, ResponseCode.NO_ENOUGH_MONEY_MESSAGE);
        }
        colaNewsBiz.donate(parentId, amount, coinCode);
        return AppResponse.ok();
    }


    @RequestMapping(value = "history", method = RequestMethod.GET)
    public AppResponse history(String limit, String timestamp, @RequestParam(value = "id") String id) {
        Long time;
        Integer limits;
        boolean isFresh;
        if (StringUtils.isBlank(timestamp) || timestamp.equals("0")) {
            isFresh = true;
            time = System.currentTimeMillis();
        } else {
            isFresh = false;
            time = Long.valueOf(timestamp);
        }
        if (StringUtils.isBlank(limit)) {
            limits = 15;
        } else {
            limits = Integer.valueOf(limit);
        }

        return AppResponse.ok().data(colaNewsBiz.historyList(time, limits, id, isFresh));
    }

    @RequestMapping(value = "setWeight", method = RequestMethod.POST)
    public AppResponse setWeight(@RequestBody Map<String, String> params) {

        String id = params.get("id");
        Long weight = Long.valueOf(params.get("weight"));
        if (StringUtils.isBlank(id) || weight <= 0) {
            return AppResponse.paramsError();
        }
        colaNewsBiz.setWeight(id, weight);
        return AppResponse.ok();
    }

    @RequestMapping(value = "comment", method = RequestMethod.POST)
    public AppResponse comment(@RequestBody Map<String, String> params) {
        String parentId = params.get("parentId");
        String content = params.get("content");
        if (content.length()>150){
            AppResponse.error(ColaLanguage.get(ColaLanguage.STRING_LIMIT));
        }
        colaNewsBiz.comment(parentId, content);
        return AppResponse.ok();
    }

    @RequestMapping(value = "deleteComment", method = RequestMethod.POST)
    public AppResponse deleteComment(@RequestBody Map<String, String> params) {
        String id = params.get("id");
        colaNewsBiz.deleteComment(id);
        return AppResponse.ok();
    }

    @RequestMapping(value = "getCommentList", method = RequestMethod.GET)
    public AppResponse getCommentList(String parentId, String limit, String timestamp) {
        Long time;
        Integer limits;
        if (StringUtils.isBlank(parentId)) {
            return AppResponse.paramsError();
        }
        if (StringUtils.isBlank(timestamp) || timestamp.equals("0")) {
            time = System.currentTimeMillis();
        } else {
            time = Long.valueOf(timestamp);
        }
        if (StringUtils.isBlank(limit)) {
            limits = 15;
        } else {
            limits = Integer.valueOf(limit);
        }
        List<NewsCommentEntity> list = colaNewsBiz.getCommentList(parentId, limits, time);
        return AppResponse.ok().data(list);
    }

    @RequestMapping(value = "publishBanner", method = RequestMethod.POST)
    public AppResponse publishBanner(@RequestBody Map<String, String> params) {
        String index = params.get("index");
        String image = params.get("image");
        String url = params.get("url");
        if (StringUtils.isBlank(image)) {
            return AppResponse.paramsError();
        }
        colaNewsBiz.publishBanner(index, image, url);
        return AppResponse.ok();
    }

    @RequestMapping(value = "deleteBanner", method = RequestMethod.POST)
    public AppResponse deleteBanner(@RequestBody Map<String, String> params) {
        String id = params.get("id");
        colaNewsBiz.deleteBanner(id);
        return AppResponse.ok();
    }

    @RequestMapping(value = "changeBanner", method = RequestMethod.POST)
    public AppResponse changeBanner(@RequestBody NewsBannerEntity entity) {
        colaNewsBiz.changeBanner(entity);
        return AppResponse.ok();
    }

    @RequestMapping(value = "getBannerList", method = RequestMethod.GET)
    public AppResponse getBannerList() {
        return AppResponse.ok().data(colaNewsBiz.getBannerList());
    }

    @RequestMapping(value = "itemList", method = RequestMethod.GET)
    public AppResponse itemList(String limit, String timestamp) {
        Long time;
        Integer limits;
        if (StringUtils.isBlank(timestamp) || timestamp.equals("0")) {
            time = System.currentTimeMillis();
        } else {
            time = Long.valueOf(timestamp);
        }
        if (StringUtils.isBlank(limit)) {
            limits = 15;
        } else {
            limits = Integer.valueOf(limit);
        }
        List<NewsItemEntity> list = colaNewsBiz.itemList(time, limits);
        return AppResponse.ok().data(list);
    }

}
