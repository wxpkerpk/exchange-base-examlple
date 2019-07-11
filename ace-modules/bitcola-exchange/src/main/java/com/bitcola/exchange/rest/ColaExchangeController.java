package com.bitcola.exchange.rest;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.exchange.biz.ColaExchangeBiz;
import com.bitcola.exchange.constant.OrderDirection;
import com.bitcola.exchange.constant.OrderStatus;
import com.bitcola.exchange.constant.OrderType;
import com.bitcola.exchange.data.MarketInfo;
import com.bitcola.exchange.data.MakeOrderParams;
import com.bitcola.exchange.dto.ColaUserBalanceVo;
import com.bitcola.exchange.message.OrderMessage;
import com.bitcola.exchange.feign.IDataServiceFeign;
import com.bitcola.exchange.mapper.OrderMapper;
import com.bitcola.exchange.queue.LinkedBlockingQueueMap;
import com.bitcola.exchange.security.auth.client.config.UserAuthConfig;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.auth.client.jwt.UserAuthUtil;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.service.AccountService;
import com.bitcola.exchange.service.KlineService;
import com.bitcola.exchange.service.MatchService;
import com.bitcola.exchange.service.RushService;
import com.bitcola.exchange.util.InFluxDbService;
import com.bitcola.exchange.util.Snowflake;
import com.bitcola.exchange.websocket.OrderNotifyEntity;
import com.bitcola.me.entity.ColaCoinSymbol;
import com.bitcola.me.entity.ColaUserLimit;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zkq
 * @create 2019-02-16 11:53
 **/
@Log4j2
@RestController
public class ColaExchangeController {

    @Resource(name = "matchOrderQueue")
    LinkedBlockingQueueMap<OrderMessage> matchOrderQueue;

    @Autowired
    ColaExchangeBiz biz;

    @Autowired
    MatchService service;

    @Autowired
    InFluxDbService inFluxDbService;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    AccountService accountService;

    @Autowired
    IDataServiceFeign dataServiceFeign;

    @Autowired
    UserAuthUtil userAuthUtil;

    @Autowired
    UserAuthConfig userAuthConfig;

    @Autowired
    KlineService klineService;

    private Snowflake snowflake = new Snowflake();
    private AtomicInteger makeOrderCount = new AtomicInteger(0);

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    /**
     * k 线
     * @param pair 交易对
     * @param end 返回这个时间点以前的指定条数,不传则默认是当前
     * @param limit 条数,最大默认 2000
     * @param type 类型
     * @return
     */
    @RequestMapping("kline")
    public AppResponse kline(String pair,Long start, Long end ,Integer limit, String type,String code ){
        if (code != null) pair = code;
        if (StringUtils.isAnyBlank(pair,type)){
            return AppResponse.paramsError();
        }
        List<Number[]> kline = biz.kline(pair, start, end, limit, type);
        return AppResponse.ok().data(kline);
    }

    /**
     * 深度
     * @return
     */
    @RequestMapping("depth")
    @Cached(name = "depth",expire = 2,cacheType = CacheType.LOCAL)
    public AppResponse depth(String pair,Integer length,Integer scale) {
        if (StringUtils.isBlank(pair)){
            return AppResponse.paramsError();
        }
        ColaCoinSymbol colaCoinSymbol = dataServiceFeign.getSymbol(pair);
        if (scale == null || scale == 0) scale = colaCoinSymbol.getPriceScale();
        if (length == null || length == 0) length = 100;
        Map<String,Object> map = new HashMap<>();
        List<BigDecimal[]> ask = MatchService.sellDepth.get(pair).getDepth(length, scale);
        Collections.reverse(ask);
        List<BigDecimal[]> bids = MatchService.buyDepth.get(pair).getDepth(length, scale);
        map.put("ask",ask);
        map.put("bids",bids);
        map.put("current",biz.getMarketByPair(pair,"-1"));
        return AppResponse.ok().data(map);
    }

    /**
     * 获取市场行情
     * @return
     */
    @RequestMapping("market")
    public AppResponse market(HttpServletRequest request,String pair, String symbol,Integer onlyFav) {
        String token = userAuthConfig.getToken(request);
        String userId = "-1";
        if (StringUtils.isNotBlank(token)){
            try {
                userId = userAuthUtil.getInfoFromToken(token).getId();
            } catch (Exception e) {
                log.error("token 错误");
            }
        }
        if (onlyFav != null && onlyFav == 1){
            return AppResponse.ok().data(biz.getMarketByFav(userId));
        }
        if (StringUtils.isNotBlank(pair)){
            MarketInfo data = biz.getMarketByPair(pair,userId);
            return AppResponse.ok().data(data);
        }
        if (StringUtils.isNotBlank(symbol)){
            List<MarketInfo> data = biz.getMarketBySymbol(symbol,userId);
            return AppResponse.ok().data(data);
        }
        List<Map<String,Object>> list = biz.getMarketAll(userId);
        return AppResponse.ok().data(list);
    }
    /**
     * 获取市场行情
     * @return
     */
    @RequestMapping("marketAll")
    public AppResponse marketAll(HttpServletRequest request){
        String token = userAuthConfig.getToken(request);
        String userId = "-1";
        if (StringUtils.isNotBlank(token)){
            try {
                userId = userAuthUtil.getInfoFromToken(token).getId();
            } catch (Exception e) {
                log.error("token 错误");
            }
        }
        List<String> symbols = dataServiceFeign.getSymbols();
        List<MarketInfo> list = new ArrayList<>();
        for (String symbol : symbols) {
            list.addAll(biz.getMarketBySymbol(symbol, userId));
        }
        return AppResponse.ok().data(list);
    }
    /**
     * 获取市场行情
     * @return
     */
    @RequestMapping("marketByPair")
    public AppResponse market(HttpServletRequest request,String pair){
        String token = userAuthConfig.getToken(request);
        String userId = "-1";
        if (StringUtils.isNotBlank(token)){
            try {
                userId = userAuthUtil.getInfoFromToken(token).getId();
            } catch (Exception e) {
                log.error("token 错误");
            }
        }
        if (StringUtils.isNotBlank(pair)){
            MarketInfo data = biz.getMarketByPair(pair,userId);
            return AppResponse.ok().data(data);
        }
        return AppResponse.paramsError();
    }


    /**
     * 48小时趋势
     * @param pair
     * @return
     */
    @RequestMapping(value = "/getTendency", method = RequestMethod.GET)
    @Cached(cacheType = CacheType.LOCAL, expire = 10)
    public AppResponse getTendency(String pair) {
        List<Number> numbers = inFluxDbService.getTendency(pair);
        return AppResponse.ok().data(numbers);
    }

    /**
     * 48小时趋势
     * @param pairs
     * @return
     */
    @RequestMapping(value = "/getTendencies", method = RequestMethod.POST)
    @Cached(cacheType = CacheType.LOCAL, expire = 10)
    public AppResponse getTendencies(@RequestBody List<String> pairs) {
        Map<String, List<Number>> tendencyMap = new HashMap<>();
        pairs.forEach(x -> {
            List<Number> numbers = inFluxDbService.getTendency(x);
            tendencyMap.put(x, numbers);
        });
        return AppResponse.ok().data(tendencyMap);
    }

    /**
     *  myToken 的获取所有市场行情
     * @return
     */
    @RequestMapping("pairAll")
    @Cached(name = "PairAll",cacheType = CacheType.LOCAL, expire = 60)
    public AppResponse pairAll() {
        List list = new ArrayList<>();
        List<String> symbols = dataServiceFeign.getSymbols();
        long currentTimeMillis = System.currentTimeMillis();
        for (String symbol : symbols) {
            List<MarketInfo> data = biz.getMarketBySymbol(symbol,"-1");
            for (MarketInfo datum : data) {
                Map<String,Object> map = new HashMap<>();
                map.put("coinSymbol",datum.getPair());
                map.put("currentPrice",datum.getPrice());
                map.put("vol24h",datum.getVol());
                if (datum.getPrice().compareTo(BigDecimal.ZERO) == 0){
                    map.put("amount24h",0);
                } else {
                    map.put("amount24h",datum.getVol().divide(datum.getPrice(),2, RoundingMode.HALF_UP));
                }
                map.put("lastUpdateTime",currentTimeMillis);
                list.add(map);
            }
        }
        return AppResponse.ok().data(list);
    }

    /**
     * 获得交易token
     * @return
     */
    @RequestMapping(value = "getTransactionSign", method = RequestMethod.POST)
    public AppResponse<HashMap> getTransactionSign(@RequestBody Map<String, String> params) {
        String token = BaseContextHandler.getToken();
        String sign = UUID.randomUUID().toString();
        String userId = BaseContextHandler.getUserID();
        String moneyPassword_real = biz.getUserMoneyPassword(userId);
        if (moneyPassword_real == null)
            return new AppResponse(ResponseCode.NO_MONEY_PASSWORD_CODE, ResponseCode.NO_MONEY_PASSWORD_MESSAGE);

        if (encoder.matches(params.get("pin"), moneyPassword_real)) {
            biz.setSign(sign, token);
            var data = new HashMap<String, Object>(2);
            data.put("token", sign);
            data.put("expire", (25 - 1) * 60 * 60 * 1000);
            return AppResponse.ok().data(data);
        } else {
            return new AppResponse(ResponseCode.PIN_ERROR_CODE, ResponseCode.PIN_ERROR_MESSAGE);
        }

    }

    @Autowired
    RushService rushService;

    /**
     * 下单
     * @param
     * @return
     */
    @RequestMapping(value = "makeOrder",method = RequestMethod.POST)
    public AppResponse makeOrder(@RequestBody MakeOrderParams makeOrderParams){
        int index = makeOrderCount.get();
        if (index > 500){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.SYSTEM_BUSY));
        }
        makeOrderCount.incrementAndGet();
        int i = 0;
        String orderId = snowflake.nextIdStr();
        try {
            // 禁止交易
            ColaUserLimit userLimit = dataServiceFeign.getUserLimit(BaseContextHandler.getUserID(), "makeOrder");
            if (userLimit!=null){
                Long limitTime = userLimit.limitTime();
                if (limitTime>System.currentTimeMillis()){
                    return AppResponse.error(ResponseCode.USER_LIMIT_CODE,
                            ColaLanguage.get(ColaLanguage.EXCHANGE_MAKE_ORDER_LIMIT));
                }
            }// 禁止交易结束

            ColaCoinSymbol colaCoinSymbol = dataServiceFeign.getSymbol(makeOrderParams.getPair());
            // 不是项目方,并且,是项目方,但是不是这个交易对
            if (rushService.isPassPairOpenTimeLimit(makeOrderParams)){
                long currentTime = System.currentTimeMillis();
                if (currentTime <= colaCoinSymbol.getOnlineTime()){
                    return new AppResponse(ResponseCode.COIN_NOT_OPEN, ResponseCode.COIN_NOT_OPEN_MESSAGE);
                }
            }
            // 限制普通用户抢购数量,禁止卖单,价格
            if (makeOrderParams.getPair().equals(rushService.getRushPair())){
                if (!BaseContextHandler.getUserID().equals(rushService.getRushProjectUserId())){
                    long currentTimeMillis = System.currentTimeMillis();
                    if (currentTimeMillis < rushService.getRushStartTime() || currentTimeMillis > rushService.getRushEndTime()){
                        return new AppResponse(ResponseCode.COIN_NOT_OPEN, ResponseCode.COIN_NOT_OPEN_MESSAGE);
                    }
                    if (makeOrderParams.getType().equals(OrderDirection.SELL)){
                        return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
                    }

                    if (rushService.isOutOfMaxLimit(makeOrderParams.getPrice().multiply(makeOrderParams.getNumber()),BaseContextHandler.getUserID())) {
                        if (ColaLanguage.getCurrentLanguage().equals(ColaLanguage.LANGUAGE_CN)) {
                            return AppResponse.error("您总购买超出项目认购额度 : " +
                                    rushService.getRushMaxLimit().stripTrailingZeros().toPlainString() + " USDT");
                        } else {
                            return AppResponse.error("Your total purchase exceeds the project subscription limit : " +
                                    rushService.getRushMaxLimit().stripTrailingZeros().toPlainString() + " USDT");
                        }
                    }
                    if (makeOrderParams.getPrice().compareTo(rushService.getCurrentRushPrice()) != 0){
                        if (ColaLanguage.getCurrentLanguage().equals(ColaLanguage.LANGUAGE_CN)) {
                            return AppResponse.error("请确认当前价格为 : "+rushService.getCurrentRushPrice()+" ");
                        } else {
                            return AppResponse.error("Please confirm the current price is : "+rushService.getCurrentRushPrice()+" ");
                        }
                    }
                }
            }

            // 交易日期,token,签名 是否正确
            if (Math.abs(makeOrderParams.getTime() - System.currentTimeMillis()) > 10 * 60 * 1000){
                return new AppResponse(ResponseCode.TIME_EXPIRE, ResponseCode.TIME_EXPIRE_MESSAGE);
            }
            String passwordSign = biz.getSign(BaseContextHandler.getToken());
            if (passwordSign == null){
                return new AppResponse(ResponseCode.SIGN_EXPIRE, ResponseCode.SIGN_EXPIRE_MESSAGE);
            }
            String rightSign = biz.makeSign(makeOrderParams, passwordSign);
            if (!makeOrderParams.getSign().equals(rightSign)) {
                return AppResponse.error(ResponseCode.SIGN_WRONG, ResponseCode.SIGN_WRONG_MESSAGE);
            }

            // 截断价格数量
            if (!assertGreaterThanZero(makeOrderParams.getPrice()) || !assertGreaterThanZero(makeOrderParams.getNumber())){
                return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
            }
            OrderMessage order = new OrderMessage();
            order.setPrice(makeOrderParams.getPrice().setScale(colaCoinSymbol.getPriceScale(), RoundingMode.DOWN));
            order.setNumber(makeOrderParams.getNumber().setScale(colaCoinSymbol.getAmountScale(), RoundingMode.DOWN));

            if (order.getPrice().compareTo(BigDecimal.ZERO)==0 || order.getNumber().compareTo(BigDecimal.ZERO)==0){
                return AppResponse.paramsError();
            }
            // 判断最大最小限制
            BigDecimal min = colaCoinSymbol.getMin();
            BigDecimal max = colaCoinSymbol.getMax();
            if (min.compareTo(BigDecimal.ZERO)!=0 && order.getNumber().compareTo(min) < 0){
                return AppResponse.error(String.format(ColaLanguage.get(ColaLanguage.EXCHANGE_MIN_LIMIT), min.stripTrailingZeros().toPlainString()));
            }
            if (max.compareTo(BigDecimal.ZERO)!=0 && order.getNumber().compareTo(max) > 0){
                return AppResponse.error(String.format(ColaLanguage.get(ColaLanguage.EXCHANGE_MAX_LIMIT), max.stripTrailingZeros().toPlainString()));
            }

            order.setRemain(order.getNumber());
            order.setId(orderId);
            order.setTimestamp(System.currentTimeMillis());
            order.setUserId(BaseContextHandler.getUserID());
            order.setType(OrderType.LIMIT);
            order.setDirection(makeOrderParams.getType());
            order.setPair(makeOrderParams.getPair());
            if (!MatchService.running.get(order.getPair())){
                throw new RuntimeException("撮合未启动");
            }
            i = biz.makeOrder(order, colaCoinSymbol.getFees());
            if (i == -1) {
                return AppResponse.error(ResponseCode.NO_ENOUGH_MONEY_CODE, ResponseCode.NO_ENOUGH_MONEY_MESSAGE);
            }
            if (i == 0) {
                log.error("下单失败");
                return AppResponse.ok().data(orderId);
            }
            matchOrderQueue.putMessage(order.getPair(),order);
            ColaExchangeController.count.incrementAndGet();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            makeOrderCount.decrementAndGet();
        }
        return AppResponse.ok().data(orderId);
    }





    /**
     * 取消订单
     * @param params
     * @return
     */
    @RequestMapping(value = "cancelOrder", method = RequestMethod.POST)
    public AppResponse cancelOrder(@RequestBody Map<String, String> params) {
        count.incrementAndGet();
        int index = makeOrderCount.get();
        if (index > 500){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.SYSTEM_BUSY));
        }
        makeOrderCount.incrementAndGet();
        try {
            String userId = BaseContextHandler.getUserID();
            String orderId = params.get("orderId");
            String pair = params.get("pair");
            if (StringUtils.isBlank(orderId) && StringUtils.isNotBlank(pair)){
                // 批量撤单
                List<OrderMessage> orders = orderMapper.selectOrderByPair(pair,userId);
                for (OrderMessage order : orders) {
                    biz.cancelOrder(order);
                }
            } else {
                OrderMessage entity = orderMapper.selectByPrimaryKey(orderId);
                if (!entity.getUserId().equals(userId)){
                    return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
                }
                if (entity.getStatus().equals(OrderStatus.FULL_COMPLETED) || entity.getStatus().equals(OrderStatus.FULL_CANCELLED) || entity.getStatus().equals(OrderStatus.PARTIAL_CANCELLED)){
                    return AppResponse.ok();
                }
                biz.cancelOrder(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            makeOrderCount.decrementAndGet();
        }
        return AppResponse.ok();
    }



    /**
     * 获得市场订单(查询数据库即可)
     * @return
     */
    @RequestMapping("getMarketOrder")
    public AppResponse getMarketOrder(String pair,Integer size){
        if (size == null|| size == 0){
            size = 20;
        }
        List<OrderNotifyEntity> list = biz.getMarketOrder(pair,size);
        return AppResponse.ok().data(list);
    }

    /**
     *  查询数据库即可
     * @return 返回交易和交易明细
     */
    @RequestMapping("getPersonOrder")
    public AppResponse getPersonOrder(String pair, Long timestamp, Integer size, String type, Integer isPending){
        String userId = BaseContextHandler.getUserID();
        if (timestamp == null || timestamp == 0) {
            timestamp = System.currentTimeMillis();
        }
        if (isPending == null || isPending == 0) {
            isPending = 0;
        }
        if (size == null || size == 0) {
            size = 10;
        }
        List<OrderMessage> list = biz.getPersonOrder(userId, timestamp, pair, type, size, isPending);
        return AppResponse.ok().data(list);
    }

    /**
     *  查询数据库即可
     * @return 返回交易和交易明细
     */
    @RequestMapping("getPersonOrderDetail")
    public AppResponse getPersonOrderDetail(String orderId){
        Map<String, Object> list = biz.getPersonOrderDetail(orderId);
        return AppResponse.ok().data(list);
    }



    /**
     * 查询数据库即可
     * @return
     */
    @RequestMapping("orderManagement")
    public TableResultResponse orderManagement(String pair, String state, Integer page, Integer size, String type,
                                               Long startTime, Long endTime, String pairL, String pairR){
        String userId = BaseContextHandler.getUserID();
        if (page == null || page == 0) {
            page = 1;
        }
        if (size == null || size == 0) {
            size = 10;
        }
        return biz.orderManagement(userId, pair, state, page, size, type, startTime, endTime, pairL, pairR);
    }

    /**
     * 获得交易对余额
     * @param pair
     * @return
     */
    @RequestMapping("getBalance")
    public AppResponse getBalance(String pair,String coinCode){
        ColaUserBalanceVo[] balanceVos =  biz.getBalance(pair,coinCode);
        return AppResponse.ok().data(balanceVos);
    }

    /**
     * 获得交易对或者币种的价格
     * @param coinCode
     * @param pair
     * @return
     */
    @RequestMapping("getCoinPrice")
    @Cached(name = "coinPrice",cacheType = CacheType.LOCAL, expire = 10)
    public AppResponse getCoinPrice(String coinCode,String pair){
        if (StringUtils.isNotBlank(coinCode)){
            BigDecimal price = biz.getCoinPrice(coinCode);
            return AppResponse.ok().data(price);
        } else if (StringUtils.isNotBlank(pair)){
            return AppResponse.ok().data(biz.getPairPrice(pair));
        }
        return AppResponse.paramsError();
    }


    /**
     * 新开一个交易对
     * @param pair
     * @return
     */
    @RequestMapping("admin/addPair")
    public boolean addPair(String pair){
        if (StringUtils.isBlank(pair)) return false;
         return biz.addPair(pair);
    }

    private boolean assertGreaterThanZero(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return true;
    }


    public static AtomicLong count = new AtomicLong(0);
    private long rate = 0;

    @RequestMapping("getTps")
    public AppResponse getTps(){
        return AppResponse.ok().data(rate);
    }

    @Scheduled(cron = "*/1 * * * * ?")
    public void countTps(){
        rate = count.get();
        count.set(0);
    }



}
