package com.bitcola.exchange.ctc.mapper;

import com.bitcola.exchange.ctc.entity.ColaUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ColaUserMapper {


    ColaUser getUserInfo(@Param("userId") String userId);
}
