package com.bitcola.exchange.security.community.repostory;

import com.bitcola.exchange.security.community.entity.NewsBannerEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NewsBannerRepository extends MongoRepository<NewsBannerEntity,String> {

}
