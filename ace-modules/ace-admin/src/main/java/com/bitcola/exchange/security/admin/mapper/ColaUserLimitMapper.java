package com.bitcola.exchange.security.admin.mapper;

import com.bitcola.me.entity.ColaUserLimit;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface ColaUserLimitMapper extends Mapper<ColaUserLimit> {
    ColaUserLimit getUserLimit(@Param("userId") String userId, @Param("module") String module);
}
