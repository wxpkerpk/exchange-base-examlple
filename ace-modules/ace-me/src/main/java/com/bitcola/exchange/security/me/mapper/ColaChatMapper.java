package com.bitcola.exchange.security.me.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ColaChatMapper {
    void newGroup(@Param("id") String id, @Param("avatar") String avatar, @Param("groupName")String groupName);

    List<Map<String, String>> groupInfo(@Param("ids") List<String> ids);

    void updateGroup(@Param("id") String id, @Param("avatar") String avatar, @Param("groupName")String groupName);

    Integer checkGroupNameRepeat(@Param("groupName")String groupName);
}
