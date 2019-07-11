package com.bitcola.activity.mapper;

import com.bitcola.activity.entity.ColaIso;
import com.bitcola.activity.entity.ColaIsoStage;
import com.bitcola.activity.entity.ColaIsoUnlockLog;
import com.bitcola.activity.vo.ColaIsoLastResponse;
import com.bitcola.activity.vo.ColaIsoRankResponse;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface ColaIsoMapper extends Mapper<ColaIso> {

    BigDecimal countNumber();

    BigDecimal capitalPool();

    List<ColaIsoRankResponse> rank(@Param("limit") Integer limit);
    List<ColaIsoRankResponse> inviterRank(@Param("limit") Integer limit);

    List<ColaIsoLastResponse> last(@Param("limit") Integer limit);

    String getDepositAddress(@Param("userId")String userId, @Param("symbol")String symbol);

    String getUserPin(@Param("userId") String userID);

    int frozenBalance(@Param("amount")BigDecimal amount, @Param("id")String id, @Param("key")String balanceKey);

    int subFrozenAndBack(@Param("id")String id, @Param("totalAmount")BigDecimal totalAmount, @Param("remainAmount")BigDecimal remainAmount,@Param("key")String balanceKey);

    int addCoinCode(@Param("id")String id, @Param("totalNumber")BigDecimal totalNumber,@Param("key")String balanceKey);

    void record(List<ColaIso> record);

    Long getStartTime();

    ColaIsoRankResponse selfRank(@Param("userId") String userID);

    BigDecimal selectBalanceNumberById(@Param("id")String id);

    void insertLockCoin(@Param("id")String id, @Param("number")BigDecimal totalNumber);

    int updateLockCoin(@Param("id")String id, @Param("number")BigDecimal totalNumber);

    void subLockNumber(@Param("id")String id, @Param("number")BigDecimal unLockNumber);

    String getInviterUserId(@Param("userId")String userId);


    void batchInsertUnlockLog(List<ColaIsoUnlockLog> logs);


    BigDecimal getUserBalance(@Param("userId")String userId, @Param("coinCode")String coinCode);

    Map<String, BigDecimal> getLockNumber(@Param("id")String id);

    List<ColaIsoUnlockLog> getUnlockDetail(@Param("userId")String userId);

    Map<String, BigDecimal> getTotalNumberAndAmount(@Param("userId")String inviterUserId);

    List<Map<String,Object>> getUsersLockNumber();

    List<Map<String,Object>> getRoundOneReward();

    Integer selectKycStatus(@Param("userId")String userId);
}
