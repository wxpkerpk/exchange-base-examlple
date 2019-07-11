package com.bitcola.exchange.security.admin.mapper;

import com.bitcola.ctc.ColaCtcOrder;
import com.bitcola.exchange.security.common.util.AdminQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ColaCtcOrderMapper extends Mapper<ColaCtcOrder> {
    List<ColaCtcOrder> list(AdminQuery query);

    Long count(AdminQuery query);

    String getPin(@Param("userId") String userId);

    int buySub(@Param("id")String id, @Param("number")BigDecimal number, @Param("key")String balanceKey);

    int buyAdd(@Param("id")String id, @Param("number")BigDecimal number, @Param("key")String balanceKey);

    int sellSub(@Param("id")String id, @Param("number")BigDecimal number, @Param("key")String balanceKey);

    int sellAdd(@Param("id")String id, @Param("number")BigDecimal number, @Param("key")String balanceKey);

    int unFrozen(@Param("id")String id, @Param("number")BigDecimal number, @Param("key")String balanceKey);

    String getTelephone(@Param("userId") String userId);
}
