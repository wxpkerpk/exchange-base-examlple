package com.bitcola.exchange.mapper;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.exchange.data.MarketInfo;
import com.bitcola.exchange.dto.ColaUserBalanceVo;
import com.bitcola.exchange.entity.MatchRecord;
import com.bitcola.exchange.message.OrderMessage;
import com.bitcola.exchange.websocket.OrderNotifyEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ColaExchangeMapper {

    @Cached(name = "getFirstSymbolByCoinCode",cacheType = CacheType.LOCAL, expire = 60)
    String getFirstSymbolByCoinCode(@Param("coinCode") String coinCode);

    ColaUserBalanceVo getUserBalance(@Param("id")String id);

    String getUserMoneyPassword(@Param("userId")String userId);

    @Cached(name = "marketInfo",cacheType = CacheType.LOCAL, expire = 3)
    List<MarketInfo> getMarketInfoByCoinCodeSymbol(@Param("coinCode") String coinCode, @Param("symbol") String symbol,
                                                   @Param("userId")String userId,@Param("onlyFav")int onlyFav);

    @Cached(name = "getMarketOrder",cacheType = CacheType.LOCAL, expire = 3)
    List<OrderNotifyEntity> getMarketOrder(@Param("pair")String pair, @Param("size")Integer size);

    List<OrderMessage> orderManagement(@Param("userId") String userId, @Param("code") String code, @Param("state") String state,
                                              @Param("page") Integer page, @Param("size") Integer size, @Param("type") String type,
                                              @Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("pairL") String pairL,
                                              @Param("pairR") String pairR);

    Long countOrderManagement(@Param("userId") String userId, @Param("code") String code, @Param("state") String state,
                              @Param("type") String type, @Param("startTime") Long startTime, @Param("endTime") Long endTime,
                              @Param("pairL") String pairL, @Param("pairR") String pairR);

    List<OrderMessage> getPersonOrder(@Param("userId") String userId, @Param("timestamp") Long timestamp,
                                     @Param("code") String code,
                                     @Param("type") String type, @Param("size") Integer size, @Param("isPending") Integer isPending);

    @Cached(cacheType = CacheType.LOCAL, expire = 2)
    List<MatchRecord> personOrderDetail(@Param("orderId")String orderId);

    @Cached(name = "getAllPair",cacheType = CacheType.LOCAL, expire = 60)
    List<String> getAllPair();

    List<Map<String, String>> getInviterAll(@Param("page")int page, @Param("size")int size);

    List<Map<String, Object>> getYSTUsdtNumber(@Param("pair")String pair);

}
