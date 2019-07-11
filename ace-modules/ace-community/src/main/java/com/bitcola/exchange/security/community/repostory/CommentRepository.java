package com.bitcola.exchange.security.community.repostory;

import com.bitcola.exchange.security.community.entity.CommentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentRepository extends MongoRepository<CommentEntity,String> {
}
