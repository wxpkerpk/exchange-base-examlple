package com.bitcola.exchange.ctc.mapper;

import com.bitcola.ctc.ColaCtcOrder;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ColaCtcMapper extends Mapper<ColaCtcOrder> {
    List<ColaCtcOrder> list(@Param("userId") String userID, @Param("size")Integer size, @Param("cursor")Long cursor,
                            @Param("direction")String direction, @Param("status")String status,
                            @Param("startTime")Long startTime,@Param("endTime")Long endTime,
                            @Param("isPending")Integer isPending
    );

    int frozenUserBalance(@Param("id") String id,@Param("number")BigDecimal number,@Param("key")String key);

    List<String> getNotifyTelephone(@Param("module")String module);
}
