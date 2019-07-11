package com.bitcola.dataservice.mapper;

import com.bitcola.me.entity.ColaUserLimit;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ColaUserLimitMapper {
    ColaUserLimit getUserLimit(@Param("userId") String userId, @Param("module")String module);
}
