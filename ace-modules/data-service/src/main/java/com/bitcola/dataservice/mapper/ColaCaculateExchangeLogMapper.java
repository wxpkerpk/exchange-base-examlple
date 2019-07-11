package com.bitcola.dataservice.mapper;

import com.bitcola.caculate.entity.ColaOrder;
import com.bitcola.caculate.entity.ExchangeLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ColaCaculateExchangeLogMapper extends Mapper<ExchangeLog> {
    List<ExchangeLog> selectByUserId(@Param(value = "code")String code,@Param(value = "userid") String userId, @Param(value = "start")int start, @Param(value = "size")int size);
    List<ExchangeLog> selectByCode(@Param(value = "code")String code,@Param(value = "start")int start,@Param(value = "size")int size);

    List<ExchangeLog> selectById(@Param(value = "orderId")String orderId);
    void insertBatch(List<ExchangeLog>exchangeLogs);
}
