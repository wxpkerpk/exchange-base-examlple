package com.bitcola.exchange.security.admin.mapper;

import com.bitcola.exchange.security.admin.entity.User;
import com.bitcola.exchange.security.common.util.AdminQuery;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserMapper extends Mapper<User> {
    public List<User> selectMemberByGroupId(@Param("groupId") String groupId);
    public List<User> selectLeaderByGroupId(@Param("groupId") String groupId);

    List<User> page(AdminQuery query);

    Long count(AdminQuery query);

    Long getMaxId();
}