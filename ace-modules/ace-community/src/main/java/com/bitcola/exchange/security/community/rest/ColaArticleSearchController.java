package com.bitcola.exchange.security.community.rest;

import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.community.biz.ColaArticleSearchBiz;
import com.bitcola.exchange.security.community.entity.ArticleItemEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 文章搜索
 *
 * @author zkq
 * @create 2018-09-16 17:07
 **/
@RestController
@RequestMapping("articleSearch")
public class ColaArticleSearchController {

    @Autowired
    ColaArticleSearchBiz searchBiz;

    @RequestMapping("search")
    public AppResponse search(String keyWord, HttpServletRequest request,Integer page,Integer size) throws Exception{
        String authorization = request.getHeader("Authorization");
        List<ArticleItemEntity> list = searchBiz.search(keyWord,authorization,page,size);
        return AppResponse.ok().data(list);
    }
}
