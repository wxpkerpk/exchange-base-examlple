package com.bitcola.exchange.security.community.repostory;

import com.bitcola.exchange.security.community.entity.ArticleEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 长文
 *
 * @author zkq
 * @create 2018-08-22 15:45
 **/
public interface ArticleRepository extends MongoRepository<ArticleEntity,String> {
}
