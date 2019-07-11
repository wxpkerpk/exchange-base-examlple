package com.bitcola.exchange.security.community.repostory;

import com.bitcola.exchange.security.community.entity.NewsCommentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NewsCommentRepository extends MongoRepository<NewsCommentEntity,String> {
}
