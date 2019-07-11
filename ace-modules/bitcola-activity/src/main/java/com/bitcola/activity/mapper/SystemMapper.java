package com.bitcola.activity.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SystemMapper {
    List<String> selectAllUserId();

    void initInnerTestUserBalance(@Param("id") String id, @Param("coin") String coin, @Param("number") BigDecimal number, @Param("key") String balanceKey);
}
