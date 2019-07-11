package com.bitcola.exchange.security.community.repostory;

import com.bitcola.exchange.security.community.entity.ArticleItemEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


public interface ArticleSearchRepository extends ElasticsearchRepository<ArticleItemEntity,String> {

}
