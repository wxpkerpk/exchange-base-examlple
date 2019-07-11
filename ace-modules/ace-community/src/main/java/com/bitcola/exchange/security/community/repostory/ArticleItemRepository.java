package com.bitcola.exchange.security.community.repostory;

import com.bitcola.exchange.security.community.entity.ArticleItemEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

/**
 * 列表
 */
public interface ArticleItemRepository extends MongoRepository<ArticleItemEntity,String> {

    List<ArticleItemEntity> findByFromUserIn(Collection<String> fromUserList, Pageable pageable);
}
