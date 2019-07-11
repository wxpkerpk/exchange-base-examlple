package com.bitcola.exchange.security.admin.mapper;

import com.bitcola.me.entity.ColaUserKyc;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.Map;

@Repository
public interface ColaKycMapper extends Mapper<ColaUserKyc> {
    Map<String, Object> kycDetail(@Param("userId") String userId);
}
