package com.bitcola.exchange.security.community.biz;

import com.bitcola.exchange.security.auth.client.jwt.UserAuthUtil;
import com.bitcola.exchange.security.auth.common.util.jwt.IJWTInfo;
import com.bitcola.exchange.security.community.entity.ArticleItemEntity;
import com.bitcola.exchange.security.community.entity.LikeEntity;
import com.bitcola.exchange.security.community.repostory.ArticleItemRepository;
import com.bitcola.exchange.security.community.repostory.ArticleSearchRepository;
import com.bitcola.exchange.security.community.util.AddUserInfoUtil;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.ExponentialDecayFunctionBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 文章搜索
 *
 * @author zkq
 * @create 2018-09-16 17:20
 **/
@Service
public class ColaArticleSearchBiz {

    @Autowired
    ArticleItemRepository itemRepository;

    @Autowired
    ArticleSearchRepository searchRepository;


    @Autowired
    ColaFeedBiz feedBiz;

    @Autowired
    UserAuthUtil userAuthUtil;


    @Autowired
    AddUserInfoUtil addUserInfoUtil;



    public List<ArticleItemEntity> search(String keyWord, String token, Integer page, Integer size) throws Exception{
        if (page == null){
            page = 0;
        }
        if (size == null) size = 10;
        Pageable pageable = PageRequest.of(page,size);

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        BoolQueryBuilder filter = queryBuilder.filter(QueryBuilders.queryStringQuery(keyWord));
        //原点（origin）：该字段最理想的值，这个值可以得到满分（1.0）
        long origin = System.currentTimeMillis();
        //偏移量（offset）：与原点相差在偏移量之内的值也可以得到满分
        long offset = 7*24*60*60*1000;
        //衰减规模（scale）：当值超出了原点到偏移量这段范围，它所得的分数就开始进行衰减了，衰减规模决定了这个分数衰减速度的快慢
        long scale = 1;
        //衰减值（decay）：该字段可以被接受的值（默认为 0.5），相当于一个分界点，具体的效果与衰减的模式有关
        double decay = 0.5;
        ExponentialDecayFunctionBuilder time = ScoreFunctionBuilders.exponentialDecayFunction("time", origin, offset, scale, decay);

        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(filter,time);
        Iterable<ArticleItemEntity> search = searchRepository.search(functionScoreQueryBuilder,pageable);
        List<String> ids = new ArrayList<>();
        for (ArticleItemEntity articleItemEntity : search) {
            ids.add(articleItemEntity.getId());
        }
        Iterable<ArticleItemEntity> allById = itemRepository.findAllById(ids);
        List<ArticleItemEntity> list = new ArrayList<>();
        for (ArticleItemEntity entity : allById) {
            list.add(entity);
        }
        addUserInfoUtil.articleItems(list);
        if (StringUtils.isBlank(token)){
            return list;
        }
        IJWTInfo infoFromToken = userAuthUtil.getInfoFromToken(token);
        List<String> articleIds = new ArrayList<>();
        for (ArticleItemEntity item : list) {
            articleIds.add(item.getId());
        }
        List<LikeEntity> liked = feedBiz.likedList(articleIds, infoFromToken.getId());
        for (LikeEntity likeEntity : liked) {
            String articleId = likeEntity.getParentId();
            for (ArticleItemEntity item : list) {
                if (articleId.equals(item.getId())){
                    item.setIsLiked(1);
                }
            }
        }
        return list;
    }

}
