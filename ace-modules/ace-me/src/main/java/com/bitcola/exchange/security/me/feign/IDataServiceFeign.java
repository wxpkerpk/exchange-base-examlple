package com.bitcola.exchange.security.me.feign;


import com.bitcola.community.entity.ArticleItemEntity;
import com.bitcola.me.entity.ColaUserLimit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2018-10-31 17:46
 **/
@Repository
@FeignClient(value = "dataservice")
public interface IDataServiceFeign {

    @RequestMapping(value = "keyWordLimit/contain",method = RequestMethod.GET)
    public boolean contain(@RequestParam("str") String str);


    @RequestMapping(value = "systemBalance/in",method = RequestMethod.GET)
    public boolean systemBalanceIn(@RequestParam("userId") String userId, @RequestParam("amount")BigDecimal amount,
                                   @RequestParam("coinCode")String coinCode,
                      @RequestParam("type")String type, @RequestParam("description")String description);
    @RequestMapping(value = "systemBalance/out",method = RequestMethod.GET)
    public boolean systemBalanceOut(@RequestParam("userId") String userId, @RequestParam("amount")BigDecimal amount,
                                   @RequestParam("coinCode")String coinCode,
                      @RequestParam("type")String type, @RequestParam("description")String description);

    @RequestMapping(value = "userLimit/getUserLimit",method = RequestMethod.GET)
    public ColaUserLimit getUserLimit(@RequestParam("userId")String userId, @RequestParam("module")String module);


    @RequestMapping(value = "user/isFollowed",method = RequestMethod.GET)
    public int isFollowed(@RequestParam("userId")String userId, @RequestParam("toUserId")String toUserId);

    /**
     * 粉丝
     * @param userId
     * @return
     */
    @RequestMapping(value = "user/getFollowedByUserId",method = RequestMethod.GET)
    public long getFollowedByUserId(@RequestParam("userId")String userId);

    /**
     * 文章数
     * @param userId
     * @return
     */
    @RequestMapping(value = "user/getPostsByUserId",method = RequestMethod.GET)
    public long getPostsByUserId(@RequestParam("userId")String userId);

    /**
     * 关注
     * @param userId
     * @return
     */
    @RequestMapping(value = "user/getFollowingByUserId",method = RequestMethod.GET)
    public long getFollowingByUserId(@RequestParam("userId")String userId);


    @RequestMapping(value = "user/getArticleItemEntity",method = RequestMethod.GET)
    public ArticleItemEntity getArticleItemEntity(@RequestParam("userId")String userId, @RequestParam("timestamp")long timestamp);
}
