package com.bitcola.exchange.security.community.repostory;

import com.bitcola.exchange.security.community.entity.NewsEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NewsRepository extends MongoRepository<NewsEntity,String> {
}
