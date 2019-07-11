package com.bitcola.exchange.biz;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.exchange.constant.*;
import com.bitcola.exchange.data.MarketInfo;
import com.bitcola.exchange.data.MakeOrderParams;
import com.bitcola.exchange.dto.ColaCoinSymbolDto;
import com.bitcola.exchange.dto.ColaUserBalanceVo;
import com.bitcola.exchange.entity.MatchRecord;
import com.bitcola.exchange.mapper.BalanceMapper;
import com.bitcola.exchange.message.OrderMessage;
import com.bitcola.exchange.feign.IDataServiceFeign;
import com.bitcola.exchange.mapper.ColaExchangeMapper;
import com.bitcola.exchange.mapper.OrderMapper;
import com.bitcola.exchange.queue.LinkedBlockingQueueMap;
import com.bitcola.exchange.security.auth.client.jwt.UserAuthUtil;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.MD5Utils;
import com.bitcola.exchange.security.common.util.ReflectionUtils;
import com.bitcola.exchange.service.*;
import com.bitcola.exchange.util.InFluxDbService;
import com.bitcola.exchange.util.KlineUtil;
import com.bitcola.exchange.websocket.OrderNotifyEntity;
import com.bitcola.me.entity.ColaCoinSymbol;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author zkq
 * @create 2019-02-16 13:10
 **/
@Service
public class ColaExchangeBiz {

    @Autowired
    ColaExchangeMapper mapper;


    @Autowired
    BalanceMapper balanceMapper;

    @Autowired
    OrderMapper orderMapper;

    @Resource(name = "matchOrderQueue")
    LinkedBlockingQueueMap<OrderMessage> matchOrderQueue;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    UserAuthUtil userAuthUtil;

    @Autowired
    IDataServiceFeign dataServiceFeign;

    @Autowired
    InFluxDbService inFluxDbService;

    @Autowired
    AccountService accountService;

    @Autowired
    KlineService klineService;

    @Autowired
    MatchService matchService;
    @Autowired
    ClearService clearService;
    @Autowired
    NotifyService notifyService;


    @Cached(cacheType = CacheType.LOCAL, expire = 10)
    public BigDecimal getCoinPrice(String coinCode) {
        if (CoinCode.USDT.equalsIgnoreCase(coinCode)){
            return BigDecimal.ONE;
        } else {
            ColaCoinSymbolDto pairDto = this.getFirstPairByCoinCode(coinCode);
            if (pairDto == null){
                return BigDecimal.ZERO;
            }
            return this.getPairPrice(pairDto.getCoinCode() + "_" + pairDto.getSymbol());
        }
    }

    @Cached(name = "getPairPrice",cacheType = CacheType.LOCAL, expire = 10)
    public BigDecimal getPairPrice(String pair) {
        BigDecimal price = klineService.getMarketPrice(pair);
        if (!CoinCode.USDT.equalsIgnoreCase(pair.split("_")[1])){
            price = price.multiply(klineService.getMarketPrice(pair.split("_")[1] + "_" + CoinCode.USDT));
        }
        return price;
    }

    @Cached(cacheType = CacheType.LOCAL, expire = 60)
    public ColaCoinSymbolDto getFirstPairByCoinCode(String coinCode) {
        String symbol =  mapper.getFirstSymbolByCoinCode(coinCode);
        if (symbol == null) return null;
        ColaCoinSymbolDto coinSymbol = new ColaCoinSymbolDto();
        coinSymbol.setCoinCode(coinCode);
        coinSymbol.setSymbol(symbol);
        return coinSymbol;
    }

    public ColaUserBalanceVo[] getBalance(String pair, String coinCode) {
        if (StringUtils.isNotBlank(coinCode)){
            ColaUserBalanceVo coin = mapper.getUserBalance(BaseContextHandler.getUserID()+coinCode);
            return new ColaUserBalanceVo[]{coin};
        }
        ColaCoinSymbol coinSymbol = dataServiceFeign.getSymbol(pair);
        String[] split = pair.split("_");
        ColaUserBalanceVo[] balanceVos = new ColaUserBalanceVo[2];
        ColaUserBalanceVo coin = mapper.getUserBalance(BaseContextHandler.getUserID()+split[0]);
        ColaUserBalanceVo symbol = mapper.getUserBalance(BaseContextHandler.getUserID()+split[1]);
        coin.setWorth(getPairPrice(pair));
        coin.setBalanceAvailable(coin.getBalanceAvailable().setScale(coinSymbol.getPriceScale(), RoundingMode.DOWN));
        coin.setBalanceFrozen(coin.getBalanceFrozen().setScale(coinSymbol.getPriceScale(), RoundingMode.DOWN));
        symbol.setWorth(getPairPrice(symbol.getCoinCode()+"_"+CoinCode.USDT));
        symbol.setBalanceAvailable(symbol.getBalanceAvailable().setScale(coinSymbol.getAmountScale(),RoundingMode.DOWN));
        symbol.setBalanceFrozen(symbol.getBalanceFrozen().setScale(coinSymbol.getAmountScale(),RoundingMode.DOWN));
        balanceVos[0] = coin;
        balanceVos[1] = symbol;
        return balanceVos;
    }


    public void setSign(String sign, String token) {
        stringRedisTemplate.opsForValue().set(this.signKey(token), sign,25, TimeUnit.HOURS);
    }

    public String getUserMoneyPassword(String userId) {
        return mapper.getUserMoneyPassword(userId);
    }

    public String getSign(String token) {
        return stringRedisTemplate.opsForValue().get(this.signKey(token));
    }

    private String signKey(String token){
        return ExchangeConstant.EXCHANGE_TOKEN_KEY+token;
    }

    public String makeSign(MakeOrderParams makeOrderParams, String passwordSign) {
        Field[] fields = makeOrderParams.getClass().getDeclaredFields();
        SortedMap<String, String> paramsMaps = new TreeMap<>();
        for (Field field : fields) {
            if (!field.getName().equals("sign")) {
                Object value = ReflectionUtils.getFieldValue(makeOrderParams, field.getName());
                if (value != null) {
                    String valueStr = null;
                    if (value instanceof BigDecimal) {
                        valueStr = ((BigDecimal) value).stripTrailingZeros().toPlainString();
                    } else {
                        valueStr = value.toString();
                    }
                    paramsMaps.put(field.getName(), valueStr);
                }
            }
        }
        paramsMaps.put("token", passwordSign);
        String signStr = makeSignStr(paramsMaps);
        return Objects.requireNonNull(MD5Utils.MD5(signStr)).toLowerCase();
    }

    private String makeSignStr(SortedMap<String, String> params) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value = entry.getValue();
            stringBuilder.append(entry.getKey()).append("=").append(value).append("&");
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1);

    }

    public void cancelOrder(OrderMessage entity) {
        entity.setType(OrderType.CANCEL);
        matchOrderQueue.putMessage(entity.getPair(),entity);
    }

    @Transactional
    public int makeOrder(OrderMessage order, BigDecimal feeRate) {
        String coinCode;
        BigDecimal number;
        if (OrderDirection.BUY.equals(order.getDirection())){
            coinCode = order.symbol();
            number = order.getPrice().multiply(order.getNumber());
        } else {
            coinCode = order.coinCode();
            number = order.getNumber();
        }
        balanceMapper.selectById(BaseContextHandler.getUserID()+coinCode);
        int i = balanceMapper.frozen(BaseContextHandler.getUserID()+coinCode,number, EncoderUtil.BALANCE_KEY);
        if (i == 0) {
            return -1;
        }
        order.setStatus(OrderStatus.PENDING);
        order.setFeeRate(feeRate);
        order.setAveragePrice(order.getPrice());
        int j = orderMapper.insertOrder(order);
        return j;
    }
    @Cached(name = "getMarketByPairInfoByPair",cacheType = CacheType.LOCAL,expire = 2)
    public MarketInfo getMarketByPair(String pair, String userId) {
        String[] split = pair.split("_");
        List<MarketInfo> marketInfo = mapper.getMarketInfoByCoinCodeSymbol(split[0], split[1], userId,0);
        return wiredMarketInfo(marketInfo).get(0);
    }

    private List<MarketInfo> wiredMarketInfo(List<MarketInfo> info){
        for (MarketInfo marketInfo : info) {
            MarketInfo pairInfo = klineService.getPairInfo(marketInfo.getPair());
            marketInfo.setPrice(pairInfo.getPrice());
            marketInfo.setMax_24h(pairInfo.getMax_24h());
            marketInfo.setMin_24h(pairInfo.getMin_24h());
            marketInfo.setVol(pairInfo.getVol());
            marketInfo.setGain_24(pairInfo.getGain_24());
            marketInfo.setWorth(this.getPairPrice(marketInfo.getPair()));
        }
        return info;
    }
    @Cached(name = "getMarketBySymbol",cacheType = CacheType.LOCAL,expire = 3)
    public List<MarketInfo> getMarketBySymbol(String symbol, String userId){
        List<MarketInfo> marketInfo = mapper.getMarketInfoByCoinCodeSymbol(null, symbol, userId,0);
        return wiredMarketInfo(marketInfo);
    }

    @Cached(name = "marketInfoAll",cacheType = CacheType.LOCAL,expire = 2)
    public List<Map<String,Object>> getMarketAll(String userId){
        List<Map<String,Object>> list = new ArrayList<>();
        List<String> symbols = dataServiceFeign.getSymbols();
        for (String symbol : symbols) {
            Map<String,Object> map = new HashMap<>();
            map.put("data",getMarketBySymbol(symbol, userId));
            map.put("symbol",symbol);
            list.add(map);
        }
        Map<String,Object> map = new HashMap<>();
        map.put("data",getMarketByFav(userId));
        map.put("symbol","fav");
        list.add(map);
        return list;
    }

    public List<MarketInfo> getMarketByFav(String userId){
        List<MarketInfo> info = mapper.getMarketInfoByCoinCodeSymbol(null, null, userId,1);
        return wiredMarketInfo(info);
    }

    public List<OrderNotifyEntity> getMarketOrder(String pair, Integer size) {
        return mapper.getMarketOrder(pair,size);
    }

    public TableResultResponse orderManagement(String userId, String code, String state, Integer page, Integer size, String type, Long startTime, Long endTime, String pairL, String pairR) {
        List<OrderMessage> list = mapper.orderManagement(userId, code, state, page, size, type, startTime, endTime, pairL, pairR);
        Long count = mapper.countOrderManagement(userId, code, state, type, startTime, endTime, pairL, pairR);
        return new TableResultResponse(count,list);
    }

    public List<OrderMessage> getPersonOrder(String userId, Long timestamp, String code, String type, Integer size, Integer isPending) {
        List<OrderMessage> maps = mapper.getPersonOrder(userId, timestamp, code, type, size, isPending);
        return maps;
    }

    public Map<String, Object> getPersonOrderDetail(String orderId) {
        Map<String, Object> map = new HashMap<>();
        OrderMessage order = orderMapper.selectByPrimaryKey(orderId);
        List<MatchRecord> maps = mapper.personOrderDetail(orderId);
        BigDecimal fee = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        String feeCoinCode = null;
        for (MatchRecord record : maps) {
            fee = fee.add(record.getFee());
            total = total.add(record.getPrice().multiply(record.getNumber()));
            if (feeCoinCode == null) feeCoinCode = record.getFeeCoinCode();
        }
        map.put("fee",fee);
        if (order.getDirection().equals(OrderDirection.BUY)){
            map.put("feeCoinCode",order.coinCode());
        } else {
            map.put("feeCoinCode",order.symbol());
        }
        map.put("total",total);
        map.put("totalCoinCode",order.symbol());
        map.put("pair",order.getPair());
        map.put("type",order.getType());
        map.put("amount",order.getNumber());
        map.put("completed",order.getNumber().subtract(order.getRemain()));
        map.put("averagePrice",order.getAveragePrice());
        map.put("price",order.getPrice());
        map.put("status",order.getStatus());
        map.put("direction",order.getDirection());
        map.put("record",maps);
        return map;
    }

    public boolean addPair(String pair) {
        matchService.startPairThread(pair);
        clearService.startClearThread(pair);
        notifyService.startNotifyThread(pair);
        klineService.startKlineService(pair);
        return false;
    }

    public List<Number[]> kline(String pair, Long start, Long end, Integer limit, String type) {
        if (limit == null || limit > 2000) limit = 2000;
        if (end == null || end ==0) end = System.currentTimeMillis();
        if (start == null || start ==0) start = end - limit * KlineUtil.klineTypes.get(type);
        start = getTypeStartTime(start,KlineUtil.klineTypes.get(type));
        end = getTypeEndTime(end,KlineUtil.klineTypes.get(type));
        List<Number[]> kline = inFluxDbService.queryKline(pair, start, end, type, limit);
        /*
         * 坑爹啊,卧槽,缓存数据,命中缓存取出来的数据是反向的
         */
        kline = new ArrayList<>(kline);
        kline.sort((o1,o2) -> {
            return Long.compare((long)o2[0],(long)o1[0]);
        });
        Number[] lastKline = klineService.getLastKline(pair, type);
        if (kline.size() > 0){
            Number[] numbers = kline.get(0);
            long perTime = numbers[0].longValue();
            Number close = numbers[4];
            Long s = KlineUtil.klineTypes.get(type);
            // 内存时间是在时间内的
            if (lastKline[0].longValue() >= start && lastKline[0].longValue() <= end){
                // 内存时间 != k 线最新时间
                if (lastKline[0].longValue() != perTime ){
                    while (perTime + s < lastKline[0].longValue()){
                        perTime = perTime + s;
                        kline.add(new Number[]{perTime,close,close,close,close,0});
                    }
                    kline.add(0,lastKline);
                } else {
                    // 删除 k 先最新,补充成内存
                    kline.remove(0);
                    kline.add(0,lastKline);
                }
            }
        } else {
            if (lastKline[0].longValue() > start && lastKline[0].longValue() < end){
                kline.add(0,lastKline);
            }
        }
        kline.sort((o1,o2) -> {
            return Long.compare((long)o1[0],(long)o2[0]);
        });
        return kline;
    }

    private static long getTypeStartTime(long time,long typeTime){
        return time - (time % typeTime);
    }

    private static long getTypeEndTime(long time,long typeTime){
        long l = time % typeTime;
        if (l == 0) return time;
        return time + (typeTime - l);
    }

}
