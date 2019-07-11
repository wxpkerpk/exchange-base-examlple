package com.bitcola.exchange.security.community.repostory;

import com.bitcola.exchange.security.community.entity.NewsItemEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author lky
 * @create 2019-04-23 18:48
 **/

public interface NewsItemRepository extends MongoRepository<NewsItemEntity,String> {

}
