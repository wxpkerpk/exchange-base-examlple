package com.bitcola.dataservice.mapper;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.caculate.entity.CoinChange;
import com.bitcola.me.entity.ColaCoin;
import com.bitcola.me.entity.ColaMeBalance;
import com.bitcola.me.entity.ColaSystemBalance;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户资金
 */
@Repository
public interface ColaUserBalanceMapper extends Mapper<ColaMeBalance> {

    /**
     * @param userID
     * @return
     */
    List<ColaMeBalance> info(@Param("userid") String userID);

    ColaMeBalance selectBalance(@Param("userid") String userID,@Param("code") String code);
    /**
     * 获取用户币种余额
     *
     * @param userID
     * @param coinCode
     * @return
     */
    BigDecimal getCoinNumber(@Param("userid") String userID, @Param("coincode") String coinCode);


    int addUserBanlance(@Param("userID")String userID,@Param("coinCode")String coinCode,@Param("gain")BigDecimal gain,@Param("key")String balanceKey);

    int reduceUserBanlance(@Param("userID")String userID,@Param("coinCode")String coinCode,@Param("gain")BigDecimal gain,@Param("key")String balanceKey);

    int reduceUserFrozenBanlance(@Param("userID")String userID,@Param("coinCode")String coinCode,@Param("gain")BigDecimal gain,@Param("key")String balanceKey);

    int addUserFrozenBanlance(@Param("userID")String userID,@Param("coinCode")String coinCode,@Param("gain")BigDecimal gain,@Param("key")String balanceKey);

    int systemIn(@Param("balance") ColaSystemBalance balance,@Param("key")String balanceKey);

    int systemOut(@Param("balance") ColaSystemBalance balance,@Param("key")String balanceKey);
    int setFrozenBanlanceZero(@Param("userID") String userId,@Param("coinCode")String coinCode,@Param("key")String balanceKey);

    ColaMeBalance getColaToken(@Param("userId")String userId);

    @Cached(key ="#code",expire = 60,cacheType = CacheType.LOCAL)
    ColaCoin getCoin(@Param("code")String code);

    @Cached(key ="#userId",expire = 600,cacheType = CacheType.LOCAL)
    String selectInvitor(@Param("userId") String userId);
}
