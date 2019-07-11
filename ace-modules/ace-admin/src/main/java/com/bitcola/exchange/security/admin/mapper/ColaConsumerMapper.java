package com.bitcola.exchange.security.admin.mapper;

import com.bitcola.exchange.security.admin.entity.Consumer;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColaConsumerMapper {
    void insertConsumer(@Param("id") String id, @Param("description")String description);

    List<Consumer> consumerList();

    Consumer consumerSelectById(@Param("id")String id);

    void deleteConsumer(@Param("id")String id);
}
