package com.bitcola.exchange.security.community.repostory;

import com.bitcola.exchange.security.community.entity.LiveEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LiveRepository extends MongoRepository<LiveEntity,String> {
}
