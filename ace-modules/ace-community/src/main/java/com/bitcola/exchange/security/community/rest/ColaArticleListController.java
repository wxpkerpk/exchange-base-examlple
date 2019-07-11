package com.bitcola.exchange.security.community.rest;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.community.biz.ColaArticleListBiz;
import com.bitcola.exchange.security.community.entity.ArticleItemEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 列表展示
 *
 * @author zkq
 * @create 2018-08-22 15:10
 **/
@RestController
@RequestMapping("/colaArticle")
public class ColaArticleListController {

    @Autowired
    ColaArticleListBiz biz;

    @RequestMapping("item")
    public AppResponse item(String id){
        ArticleItemEntity item = biz.item(id);
        return AppResponse.ok().data(item);
    }

    /**
     * 列表
     * @return
     */
    @RequestMapping("list")
    @Cached(key = "#limit + #timestamp", cacheType = CacheType.LOCAL, expire = 2)
    public AppResponse list(String timestamp,String limit, HttpServletRequest request) throws Exception{
        Long time;
        Integer limits;
        if (StringUtils.isBlank(timestamp)){
            time = System.currentTimeMillis();
        } else {
            time = Long.valueOf(timestamp);
        }
        if (StringUtils.isBlank(limit)){
            limits = 20;
        } else {
            limits = Integer.valueOf(limit);
        }
        String authorization = request.getHeader("Authorization");
        List<ArticleItemEntity> list = biz.list(time,authorization,limits);
        return AppResponse.ok().data(list);
    }

    /**
     * 文章推荐
     * @return
     */
    public AppResponse recommend(){
        return AppResponse.ok();
    }


    /**
     * 文章详情
     * @param id
     * @param type
     * @return
     */
    @RequestMapping("detail")
    public AppResponse detail(String id,String type,HttpServletRequest request) throws  Exception{
        if (StringUtils.isBlank(id) || StringUtils.isBlank(type)){
            return AppResponse.paramsError();
        }
        String token = request.getHeader("Authorization");
        Map<String,Object> resp = null;
        try {
            resp = biz.detail(id,type,token);
        } catch (Exception e) {
            return AppResponse.error(1001,"Deleted");
        }
        return AppResponse.ok().data(resp);
    }

    /**
     * 获得最近发表的内容
     * @param userId
     * @param timestamp
     * @return
     */
    @RequestMapping("getPostsByUserId")
    public AppResponse getPostsByUserId(String userId,Long timestamp,Integer size,HttpServletRequest request) throws Exception{
        String authorization = request.getHeader("Authorization");
        if (timestamp == null || timestamp == 0){
            timestamp = System.currentTimeMillis();
        }
        if (size == null || size == 0){
            size = 10;
        }
        List<ArticleItemEntity> list = biz.getPostsByUserId(userId,timestamp,size,authorization);
        return AppResponse.ok().data(list);
    }


    /**
     * 最近点赞的文章
     * @return
     */
    @RequestMapping("getLikeArticleList")
    public AppResponse getLikeArticleList(String userId,Long timestamp,Integer size,HttpServletRequest request) throws Exception{
        String authorization = request.getHeader("Authorization");
        if (timestamp == null || timestamp == 0){
            timestamp = System.currentTimeMillis();
        }
        if(size == null || size == 0){
            size = 10;
        }
        List<ArticleItemEntity> list = biz.getLikeArticleList(userId,timestamp,size,authorization);
        return AppResponse.ok().data(list);
    }







}
