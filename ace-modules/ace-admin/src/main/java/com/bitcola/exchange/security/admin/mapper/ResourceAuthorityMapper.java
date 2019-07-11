package com.bitcola.exchange.security.admin.mapper;

import com.bitcola.exchange.security.admin.entity.ResourceAuthority;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

public interface ResourceAuthorityMapper extends Mapper<ResourceAuthority> {
    public void deleteByAuthorityIdAndResourceType(@Param("authorityId")String authorityId,@Param("resourceType") String resourceType);
}