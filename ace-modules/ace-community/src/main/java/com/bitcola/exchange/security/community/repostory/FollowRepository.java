package com.bitcola.exchange.security.community.repostory;

import com.bitcola.exchange.security.community.entity.FollowEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * 关注
 */
public interface FollowRepository extends MongoRepository<FollowEntity,String> {

    List<FollowEntity> findByUserIdAndFollowUserId(String userId, String followUserId);

}
