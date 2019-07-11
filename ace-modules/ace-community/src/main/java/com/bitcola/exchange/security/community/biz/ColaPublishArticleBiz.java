package com.bitcola.exchange.security.community.biz;

import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.community.constant.ArticleConstant;
import com.bitcola.exchange.security.community.entity.ArticleEntity;
import com.bitcola.exchange.security.community.entity.ArticleItemEntity;
import com.bitcola.exchange.security.community.entity.ImageEntity;
import com.bitcola.exchange.security.community.entity.ShortArticleEntity;
import com.bitcola.exchange.security.community.feign.IDataServiceFeign;
import com.bitcola.exchange.security.community.repostory.ArticleItemRepository;
import com.bitcola.exchange.security.community.repostory.ArticleRepository;
import com.bitcola.exchange.security.community.repostory.ArticleSearchRepository;
import com.bitcola.exchange.security.community.repostory.ShortArticleRepository;
import com.bitcola.exchange.security.community.util.ArticleWeightCalculation;
import com.bitcola.me.entity.ColaUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 发表文章
 *
 * @author zkq
 * @create 2018-08-22 15:17
 **/
@Service
public class ColaPublishArticleBiz {

    @Autowired
    ShortArticleRepository shortArticleRepository;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    ArticleItemRepository articleItemRepository;

    @Autowired
    ArticleSearchRepository articleSearchRepository;

    @Autowired
    IDataServiceFeign dataServiceFeign;

    /**
     * 发表长文
     * @param articleEntity
     */
    public void publishArticle(ArticleEntity articleEntity) {
        articleEntity.setTitle(articleEntity.getTitle());
        // 先暂时取消敏感词过滤
        //articleEntity.setTitle(dataServiceFeign.replace(articleEntity.getTitle()));
        String id = UUID.randomUUID().toString();
        articleEntity.setId(id);
        articleEntity.setTime(System.currentTimeMillis());
        articleEntity.setFromUser(BaseContextHandler.getUserID());
        articleEntity.setFromUsername(BaseContextHandler.getUsername());
        ColaUserEntity info = dataServiceFeign.info(BaseContextHandler.getUserID());
        articleEntity.setFromUserAvatar(info.getAvatar());
        articleEntity.setFromUserSign(info.getSign());
        articleEntity.setFromNickName(info.getNickName());

        //保存文章列表
        ArticleItemEntity itemEntity = new ArticleItemEntity();
        itemEntity.setId(id);
        itemEntity.setFromUser(BaseContextHandler.getUserID());
        itemEntity.setFromUsername(BaseContextHandler.getUsername());
        itemEntity.setFromUserAvatar(info.getAvatar());
        itemEntity.setFromUserSign(info.getSign());
        itemEntity.setFromNickName(info.getNickName());
        itemEntity.setTime(System.currentTimeMillis());
        itemEntity.setType(ArticleConstant.TYPE_ARTICLE);
        itemEntity.setContent(articleEntity.getTitle());
        List<ImageEntity> images = new ArrayList<>();
        images.add(articleEntity.getTitleImage());
        itemEntity.setImages(images);
        insertArticleItem(itemEntity);

        //保存文章
        articleRepository.insert(articleEntity);
    }

    /**
     * 发表短文
     * @param shortArticleEntity
     */
    public void publishShortArticle(ShortArticleEntity shortArticleEntity) {
        // 先暂时取消敏感词过滤
        //shortArticleEntity.setContent(dataServiceFeign.replace(shortArticleEntity.getContent()));
        shortArticleEntity.setContent(shortArticleEntity.getContent());
        String id = UUID.randomUUID().toString();
        shortArticleEntity.setId(id);
        shortArticleEntity.setTime(System.currentTimeMillis());
        shortArticleEntity.setFromUser(BaseContextHandler.getUserID());
        shortArticleEntity.setFromUsername(BaseContextHandler.getUsername());
        ColaUserEntity info = dataServiceFeign.info(BaseContextHandler.getUserID());
        shortArticleEntity.setFromUserAvatar(info.getAvatar());
        shortArticleEntity.setFromUserSign(info.getSign());
        shortArticleEntity.setFromNickName(info.getNickName());
        //保存文章列表
        ArticleItemEntity itemEntity = new ArticleItemEntity();
        itemEntity.setId(id);
        itemEntity.setFromUser(BaseContextHandler.getUserID());
        itemEntity.setFromUsername(BaseContextHandler.getUsername());
        itemEntity.setTime(System.currentTimeMillis());
        itemEntity.setType(ArticleConstant.TYPE_SHORT_ARTICLE);
        itemEntity.setFromUserAvatar(info.getAvatar());
        itemEntity.setFromUserSign(info.getSign());
        itemEntity.setFromNickName(info.getNickName());
        String content = shortArticleEntity.getContent();
        if (150>=content.length()){
            itemEntity.setIsFull(1);
        } else {
            itemEntity.setIsFull(0);
        }
        itemEntity.setContent(content.substring(0,150>content.length()?content.length():150));
        itemEntity.setImages(shortArticleEntity.getImages());
        insertArticleItem(itemEntity);

        //保存短文
        shortArticleRepository.insert(shortArticleEntity);
    }


    /**
     * 保存文章列表
     * @param itemEntity
     */
    private void insertArticleItem(ArticleItemEntity itemEntity){
        //设置权重
        ArticleWeightCalculation.setWeightWithTime(itemEntity);
        articleItemRepository.insert(itemEntity);
        //加全文索引
        articleSearchRepository.save(itemEntity);
    }

}
