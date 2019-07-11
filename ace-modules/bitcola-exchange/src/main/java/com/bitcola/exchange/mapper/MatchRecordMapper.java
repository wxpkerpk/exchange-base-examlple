package com.bitcola.exchange.mapper;


import com.bitcola.exchange.entity.MatchRecord;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MatchRecordMapper extends Mapper<MatchRecord> {
    void batchInsert(List<MatchRecord> recordCollector);

    BigDecimal selectDealNumberByOrderId(@Param("orderId") String orderId);
}
