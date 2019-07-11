package com.bitcola.exchange.launchpad.mapper;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.exchange.launchpad.entity.ColaResonance;
import com.bitcola.exchange.launchpad.entity.ColaResonanceUnlockLog;
import com.bitcola.exchange.launchpad.vo.ColaResonanceLastResponse;
import com.bitcola.exchange.launchpad.vo.ColaResonanceRankResponse;
import com.bitcola.exchange.launchpad.vo.ResponseProjectListVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
public interface ColaResonanceMapper extends Mapper<ColaResonance> {

    BigDecimal countNumber(@Param("coinCode")String coinCode);

    BigDecimal capitalPool(@Param("coinCode")String coinCode);

    List<ColaResonanceRankResponse> rank(@Param("limit") Integer limit,@Param("startTime")Long startTime);

    List<ColaResonanceLastResponse> last(@Param("limit") Integer limit, @Param("coinCode")String coinCode);

    String getUserPin(@Param("userId") String userID);

    int frozenBalance(@Param("amount") BigDecimal amount, @Param("id") String id, @Param("key") String balanceKey);

    int subFrozenAndBack(@Param("id") String id, @Param("totalAmount") BigDecimal totalAmount, @Param("remainAmount") BigDecimal remainAmount, @Param("key") String balanceKey);

    int addCoinCode(@Param("id") String id, @Param("totalNumber") BigDecimal totalNumber, @Param("key") String balanceKey);

    void record(List<ColaResonance> record);

    ColaResonanceRankResponse selfRank(@Param("userId") String userID,@Param("coinCode")String coinCode);

    BigDecimal selectBalanceNumberById(@Param("id") String id);

    void insertLockCoin(@Param("id") String id, @Param("number") BigDecimal totalNumber,@Param("coinCode")String coinCode,@Param("userId")String userId);

    int updateLockCoin(@Param("id") String id, @Param("number") BigDecimal totalNumber);

    void subLockNumber(@Param("id") String id, @Param("number") BigDecimal unLockNumber);

    String getInviterUserId(@Param("userId") String userId);


    void batchInsertUnlockLog(List<ColaResonanceUnlockLog> logs);


    BigDecimal getUserBalance(@Param("userId") String userId, @Param("coinCode") String coinCode);

    Map<String, BigDecimal> getLockNumber(@Param("id") String id);

    List<ColaResonanceUnlockLog> getUnlockDetail(@Param("userId") String userId,@Param("coinCode")String coinCode);

    Map<String, BigDecimal> getTotalNumberAndAmount(@Param("userId") String inviterUserId);

    List<Map<String,Object>> getUsersLockNumber(@Param("coinCode")String coinCode);

    @Cached(cacheType = CacheType.LOCAL,expire = 1)
    Integer selectKycStatus(@Param("userId") String userId);

    @Cached(cacheType = CacheType.LOCAL,expire = 1)
    List<ResponseProjectListVo> resonanceList();

    @Cached(cacheType = CacheType.LOCAL,expire = 1,timeUnit = TimeUnit.MINUTES)
    Long getProjectStartTime(@Param("coinCode")String coinCode);
    @Cached(cacheType = CacheType.LOCAL,expire = 1,timeUnit = TimeUnit.MINUTES)
    Long getProjectEndTime(@Param("coinCode")String coinCode);

    BigDecimal capitalPoolByWeek(@Param("coinCode")String coinCode, @Param("startTime")long startTime);
}
