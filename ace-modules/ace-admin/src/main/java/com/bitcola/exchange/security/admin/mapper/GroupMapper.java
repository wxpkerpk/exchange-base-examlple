package com.bitcola.exchange.security.admin.mapper;

import com.bitcola.exchange.security.admin.entity.Group;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

public interface GroupMapper extends Mapper<Group> {
    public void deleteGroupMembersById (@Param("groupId") String groupId);
    public void deleteGroupLeadersById (@Param("groupId") String groupId);
    public void insertGroupMembersById (@Param("groupId") String groupId,@Param("userId") String userId,@Param("id")String id);
    public void insertGroupLeadersById (@Param("groupId") String groupId,@Param("userId") String userId,@Param("id")String id);
}