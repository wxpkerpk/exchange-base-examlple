package com.bitcola.exchange.mapper;

import com.bitcola.exchange.entity.Balance;
import com.bitcola.exchange.entity.BatchBalance;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface BalanceMapper extends Mapper<Balance> {
    int frozen(@Param("id") String id,  @Param("number") BigDecimal number,@Param("balanceKey")String balanceKey);

    int unFrozen(@Param("id") String id, @Param("number") BigDecimal number, @Param("back") BigDecimal back,@Param("balanceKey")String balanceKey);

    int addAsset(@Param("id") String id, @Param("number") BigDecimal number,@Param("balanceKey")String balanceKey);

    String getInviterUserId(@Param("userId")String userId);

    void batchUpdate(@Param("list") List<BatchBalance> balanceList,@Param("balanceKey")String balanceKey);

    List<Map<String, String>> getInviterUserIdList(List<String> userIds);
    List<Map<String, Object>> selectBatch(@Param("list") List<BatchBalance> balanceList);

    String selectById(@Param("id")String id);
}
