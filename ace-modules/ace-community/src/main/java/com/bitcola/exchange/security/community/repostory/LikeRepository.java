package com.bitcola.exchange.security.community.repostory;

import com.bitcola.exchange.security.community.entity.LikeEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

/**
 * 点赞
 *
 * @author zkq
 * @create 2018-08-23 13:58
 **/
public interface LikeRepository extends MongoRepository<LikeEntity,String> {

    List<LikeEntity> findByParentIdInAndFromUser(Collection<String> parentIdList,String fromUser);


}
