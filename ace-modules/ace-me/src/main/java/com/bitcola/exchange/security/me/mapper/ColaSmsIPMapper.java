package com.bitcola.exchange.security.me.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author zkq
 * @create 2019-01-04 18:09
 **/
@Repository
public interface ColaSmsIPMapper {

    int insertLog(@Param("id") String id, @Param("ip")String ip, @Param("tel")String tel,@Param("time")Long time);
}
