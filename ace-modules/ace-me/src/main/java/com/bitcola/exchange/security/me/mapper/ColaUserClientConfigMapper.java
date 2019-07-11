package com.bitcola.exchange.security.me.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ColaUserClientConfigMapper {
    String get(@Param("field")String field, @Param("userId") String userID);

    void set(@Param("field")String field, @Param("config") String config, @Param("userId") String userID);

    void insert(@Param("userId")String userId);
}
