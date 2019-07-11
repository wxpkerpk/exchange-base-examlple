package com.bitcola.exchange.security.community.repostory;

import com.bitcola.exchange.security.community.entity.DonateEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DonateRepository extends MongoRepository<DonateEntity,String> {
}
