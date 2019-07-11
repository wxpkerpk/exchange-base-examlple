package com.bitcola.activity.mapper;

import com.bitcola.activity.entity.Exchange;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface ExchangeRewardMapper extends Mapper<Exchange> {

    List<Map<String,Object>> selectItem(@Param("startTime")Long startTime,@Param("endTime") Long endTime);

    BigDecimal total();

}
