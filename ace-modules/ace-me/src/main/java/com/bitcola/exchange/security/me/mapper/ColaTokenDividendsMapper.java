package com.bitcola.exchange.security.me.mapper;

import com.bitcola.exchange.security.me.vo.ColaTokenDividendsVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ColaTokenDividendsMapper {

    List<ColaTokenDividendsVo> list(@Param("keyWord") String keyWord, @Param("page") int page, @Param("limit") int limit, @Param("userId")String userID);

    Long count(@Param("keyWord") String keyWord, @Param("userId")String userID);

    List<Map<String, Object>> getWeekTransactionFees(@Param("start") long startTimestamp, @Param(("end")) long endTimestamp);
}
