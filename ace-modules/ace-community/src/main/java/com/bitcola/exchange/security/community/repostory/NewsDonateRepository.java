package com.bitcola.exchange.security.community.repostory;

import com.bitcola.exchange.security.community.entity.NewsDonateEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author lky
 * @create 2019-04-25 18:07
 **/
public interface NewsDonateRepository extends MongoRepository<NewsDonateEntity,String> {
}
