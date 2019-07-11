package com.bitcola.exchange.caculate.rest;

/*
 * @author:wx
 * @description:交易相关接口
 * @create:2018-08-11  21:18
 */

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.caculate.entity.ColaOrder;
import com.bitcola.caculate.entity.DepthData;
import com.bitcola.caculate.entity.Kline;
import com.bitcola.exchange.caculate.config.Constant;
import com.bitcola.exchange.caculate.data.HomePagePriceLine;
import com.bitcola.exchange.caculate.data.MakeOrderParams;
import com.bitcola.exchange.caculate.data.TransactionSignPair;
import com.bitcola.exchange.caculate.dataservice.ColaBalanceService;
import com.bitcola.exchange.caculate.dataservice.ColaMeService;
import com.bitcola.exchange.caculate.dataservice.PushService;
import com.bitcola.exchange.caculate.service.ExchangeService;
import com.bitcola.exchange.caculate.service.ExchangeUtils;
import com.bitcola.exchange.caculate.service.InFluxDbService;
import com.bitcola.exchange.caculate.service.MatchService;
import com.bitcola.exchange.security.auth.client.annotation.IgnoreClientToken;
import com.bitcola.exchange.security.auth.client.annotation.IgnoreUserToken;
import com.bitcola.exchange.security.auth.client.config.UserAuthConfig;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.MD5Utils;
import com.bitcola.exchange.security.common.util.ReflectionUtils;
import com.bitcola.me.entity.ColaCoinSymbol;
import com.bitcola.me.entity.ColaUserLimit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.bitcola.exchange.security.common.util.DoubleUtinls.*;

@RestController
@RequestMapping(value = "/")
@IgnoreClientToken
public class ExchangeController {
    @Autowired
    MatchService matchService;
    @Autowired
    private UserAuthConfig userAuthConfig;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Autowired
    ExchangeService exchangeService;
    @Autowired
    @Lazy
    ColaBalanceService colaBalanceService;
    @Autowired
    InFluxDbService inFluxDbService;

    @Autowired
    @Lazy
    ColaMeService colaMeService;


    @Autowired
    @Lazy
    ExchangeUtils exchangeUtils;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    PushService pushService;

    @Autowired
    UserFavoritesController favoritesController;


    static String concatCode(String code, String symbol) {
        return code + "_" + symbol;
    }

    @RequestMapping(value = "pairAll", method = RequestMethod.GET)
    @Cached(cacheType = CacheType.LOCAL, expire = 600)
    public AppResponse pairAll(){
        List list = new ArrayList<>();
        List<String> symbols = colaMeService.getSymbols();
        for (String symbol : symbols) {
            AppResponse<List<HomePagePriceLine>> listAppResponse = homePageData(symbol);
            List<HomePagePriceLine> data = listAppResponse.getData();
            for (HomePagePriceLine datum : data) {
                Map<String,Object> map = new HashMap<>();
                map.put("coinSymbol",datum.getCode());
                map.put("currentPrice",datum.getPrice());
                map.put("vol24h",datum.getVol());
                if (datum.getPrice() == 0){
                    map.put("amount24h",0);
                } else {
                    map.put("amount24h",new BigDecimal(
                            datum.getVol()/datum.getPrice()).setScale(2, RoundingMode.HALF_UP));
                }
                map.put("lastUpdateTime",System.currentTimeMillis());
                list.add(map);
            }
        }
        return AppResponse.ok().data(list);
    }

    @RequestMapping(value = "/homePageDataAll", method = RequestMethod.GET)
    @Cached( cacheType = CacheType.LOCAL, expire = 5)
    public AppResponse homePageDataAll(){
        List list = new ArrayList<>();
        List<String> symbols = colaMeService.getSymbols();
        for (String symbol : symbols) {
            AppResponse<List<HomePagePriceLine>> listAppResponse = homePageData(symbol);
            List<HomePagePriceLine> data = listAppResponse.getData();
            Map<String,Object> map = new HashMap<>();
            map.put("symbol",symbol);
            map.put("data",data);
            list.add(map);
        }
        return AppResponse.ok().data(list);
    }
    @RequestMapping(value = "/homePageDataWithSignInAll", method = RequestMethod.GET)
    @Cached( cacheType = CacheType.LOCAL, expire = 5)
    public AppResponse homePageDataWithSignInAll(){
        List list = new ArrayList<>();
        List<String> symbols = colaMeService.getSymbols();
        for (String symbol : symbols) {
            AppResponse listAppResponse = homePageDataWithSignIn(symbol);
            Object data = listAppResponse.getData();
            Map<String,Object> map = new HashMap<>();
            map.put("symbol",symbol);
            map.put("data",data);
            list.add(map);
        }
        AppResponse<List<HomePagePriceLine>> fav = favoritesController.list();
        Map<String,Object> map = new HashMap<>();
        map.put("symbol","fav");
        map.put("data",fav.getData());
        list.add(map);
        return AppResponse.ok().data(list);
    }



    @RequestMapping(value = "/homePageData", method = RequestMethod.GET)
    @Cached(key = "#code", cacheType = CacheType.LOCAL, expire = 5)
    public AppResponse<List<HomePagePriceLine>> homePageData(String code) {

        List<HomePagePriceLine> homePagePriceLines = new ArrayList<>();

        AppResponse<List<ColaCoinSymbol>> appResponse = colaMeService.getCoinSymbolBySymbol(code);
        List<ColaCoinSymbol> list = appResponse.getData();

        // kaiqiu add worth $
        BigDecimal price = BigDecimal.ZERO;
        price = exchangeUtils.getWorth(code, price);
        //
        for (ColaCoinSymbol colaCoinSymbol : list) {
            String codes = concatCode(colaCoinSymbol.getCoinCode(), code);
            HomePagePriceLine pagePriceLine = new HomePagePriceLine(exchangeUtils.getCurrentPrice(codes), exchangeUtils.getChange(codes), codes, colaCoinSymbol.getIcon(), exchangeUtils.getVol(codes)
                    , inFluxDbService.getMaxIn24h(codes), inFluxDbService.getMinIn24h(codes), colaCoinSymbol.getSort()
            );
            var symbol = colaMeService.getSymbol(codes);
            pagePriceLine.setOpenTime(symbol.getOnlineTime());

            //
            pagePriceLine.setWorth(price.multiply(new BigDecimal(pagePriceLine.getPrice())));
            // add end

            homePagePriceLines.add(pagePriceLine);
        }
        AppResponse resp = new AppResponse<>();
        resp.setData(homePagePriceLines);
        return resp;
    }

    @RequestMapping(value = "/homePageDataWithSignIn", method = RequestMethod.GET)
    public AppResponse homePageDataWithSignIn(String code) {
        var result = homePageData(code);
        var homePageData = result.getData();
        var colaCoinUserchooses = colaMeService.list(BaseContextHandler.getUserID());
        var map = new HashMap<String, Integer>(32);
        colaCoinUserchooses.forEach(x -> map.put(x.getPair(), 1));
        homePageData.forEach(y -> y.setIsFav(map.containsKey(y.getCode())));
        return AppResponse.ok().data(homePageData);
    }


    //获取交易对的价格信息
    @RequestMapping(value = "/getPairInfo", method = RequestMethod.GET)
    @Cached(key = "#pair", cacheType = CacheType.LOCAL, expire = 2)
    public AppResponse getPairInfo(String pair) {
        HomePagePriceLine pagePriceLine = exchangeUtils.getPairDetails(pair);
        return AppResponse.ok().data(pagePriceLine);

    }


    @RequestMapping(value = "/getTendency", method = RequestMethod.GET)
    @Cached(key = "#code", cacheType = CacheType.LOCAL, expire = 10)
    public AppResponse getTendency(String code) {
        List<Number> numbers = inFluxDbService.getTrendency(code);
        return AppResponse.ok().data(numbers);
    }

    @RequestMapping(value = "/getTendencies", method = RequestMethod.POST)
    public AppResponse getTendencies(@RequestBody List<String> pairs) {
        Map<String, List<Number>> tendencyesMap = new TreeMap<>();
        pairs.forEach(x -> {
            List<Number> numbers = inFluxDbService.getTrendency(x);
            tendencyesMap.put(x, numbers);

        });
        return AppResponse.ok().data(tendencyesMap);
    }


    static String makeSignStr(SortedMap<String, String> params) {

        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value = entry.getValue();
            stringBuilder.append(entry.getKey()).append("=").append(value).append("&");
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1);

    }


    @RequestMapping(value = "/makeOrder", method = RequestMethod.POST)
    @IgnoreClientToken
    public AppResponse makeOrder(HttpServletRequest request, @RequestBody MakeOrderParams makeOrderParams) {

        // 禁止交易
        ColaUserLimit userLimit = colaMeService.getUserLimit(BaseContextHandler.getUserID(), "makeOrder");
        if (userLimit!=null){
            Long limitTime = userLimit.limitTime();
            if (limitTime>System.currentTimeMillis()){
                return AppResponse.error(ResponseCode.USER_LIMIT_CODE,
                        ColaLanguage.get(ColaLanguage.EXCHANGE_MAKE_ORDER_LIMIT));
            }
        }// 禁止交易结束

        ColaCoinSymbol colaCoinSymbol = colaMeService.getSymbol(makeOrderParams.getCode());
        long currentTime = System.currentTimeMillis();
        if (currentTime <= colaCoinSymbol.getOnlineTime())
            return new AppResponse(ResponseCode.COIN_NOT_OPEN, ResponseCode.COIN_NOT_OPEN_MESSAGE);


        if (Math.abs(makeOrderParams.getTime() - System.currentTimeMillis()) > 10 * 60 * 1000)
            return new AppResponse(ResponseCode.TIME_EXPIRE, ResponseCode.TIME_EXPIRE_MESSAGE);
        String token = request.getHeader(userAuthConfig.getTokenHeader());
        String userId = BaseContextHandler.getUserID();
        String passwordSign = getSign(userId, token);
        if (passwordSign == null) return new AppResponse(ResponseCode.SIGN_EXPIRE, ResponseCode.SIGN_EXPIRE_MESSAGE);
        String sign = makeOrderParams.getSign();

        String rightSign = makeSign(makeOrderParams, passwordSign);
        String type = makeOrderParams.getType();
        BigDecimal total = makeOrderParams.getTotal() == null ? BigDecimal.ZERO : makeOrderParams.getTotal();

        BigDecimal price = makeOrderParams.getPrice() == null ? BigDecimal.ZERO : makeOrderParams.getPrice();
        BigDecimal count = makeOrderParams.getCount() == null ? BigDecimal.ZERO : makeOrderParams.getCount();
        String code = makeOrderParams.getCode();
        if (sign.equals(rightSign)) {
            if (type.equals("buy")) {
                count = total.divide(price,20,RoundingMode.DOWN);
            }
            boolean legal = checkOrderParams(price.doubleValue(), count.doubleValue(), type);

            if (!legal) return new AppResponse(ResponseCode.PARAMS_ERROR_CODE, ResponseCode.PARAMS_ERROR_MSG);
            int result = exchangeService.makeOrder(userId, code, price, count, total, type);
            if (result == 0) return new AppResponse(ResponseCode.SUCCESS_CODE, ResponseCode.SUCCESS_MESSAGE);

            return new AppResponse(ResponseCode.NO_ENOUGH_MONEY_CODE, ResponseCode.NO_ENOUGH_MONEY_MESSAGE);


        } else {
            return new AppResponse(ResponseCode.SIGN_WRONG, ResponseCode.SIGN_WRONG_MESSAGE);
        }

    }

    public static String makeSign(@RequestBody MakeOrderParams makeOrderParams, String passwordSign) {

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
        String rightSign = Objects.requireNonNull(MD5Utils.MD5(signStr)).toLowerCase();
        return rightSign;
    }

    @RequestMapping(value = "/klines", method = RequestMethod.GET)
    @IgnoreUserToken
    @IgnoreClientToken
    public AppResponse<List<Number[]>> klines(String code, long start, int limit, String type, Long end) {
        start = (start / 1000) * 1000;
        if (end != null) {
            end = (end / 1000) * 1000;
        } else {
            end = 0l;
        }
        if (limit > 2000 || limit < 0) limit = 2000;
        List<Number[]> klines = inFluxDbService.queryKline(code, start, end, type, limit);
        AppResponse resp = new AppResponse<>();
        resp.setData(klines);
        return resp;
    }

    @RequestMapping(value = "/cancelOrder", method = RequestMethod.POST)
    @IgnoreClientToken
    public AppResponse cancelOrder(@RequestBody Map<String, String> params) {
        String orderId = params.get("orderId");
        String userId = BaseContextHandler.getUserID();

        ColaOrder colaOrder = colaBalanceService.getOrderById(orderId);
        if (userId.equals(colaOrder.getUserId())) {

            exchangeService.cancelOrder(orderId);
            return new AppResponse(ResponseCode.SUCCESS_CODE, ResponseCode.SUCCESS_MESSAGE);

        } else {

            return new AppResponse(ResponseCode.PARAMS_ERROR_CODE, ResponseCode.PARAMS_ERROR_MSG);
        }


    }

    void setSign(String userId, String sign, String token) {
        String key = "caculate.transaction:" + token;
        redisTemplate.opsForValue().set(key, sign);
        redisTemplate.expire(key, Constant.TRANSACTION_TOKEN_EXPIRE, TimeUnit.HOURS);


    }

    String getSign(String userId, String token) {
        String key = "caculate.transaction:" + token;
        return redisTemplate.opsForValue().get(key);

    }


    @RequestMapping(value = "getTransactionSign", method = RequestMethod.POST)
    public AppResponse getTransactionSign(HttpServletRequest request, @RequestBody Map<String, String> params) {
        String token = request.getHeader(userAuthConfig.getTokenHeader());
        String sign = UUID.randomUUID().toString();
        String userId = BaseContextHandler.getUserID();
        String moneyPassword_real = colaBalanceService.getUserMoneyPassword(userId);
        if (moneyPassword_real == null)
            return new AppResponse(ResponseCode.NO_MONEY_PASSWORD_CODE, ResponseCode.NO_MONEY_PASSWORD_MESSAGE);
        ;
        if (encoder.matches(params.get("moneyPassword"), moneyPassword_real)) {
            setSign(userId, sign, token);
            var data = new HashMap<String, Object>(4);
            data.put("token", sign);
            data.put("expire", (Constant.TRANSACTION_TOKEN_EXPIRE - 1) * 60 * 60 * 1000);
            return AppResponse.ok().data(data);

        } else {
            return new AppResponse(ResponseCode.PIN_ERROR_CODE, ResponseCode.PIN_ERROR_MESSAGE);
        }

    }

    @RequestMapping(value = "getOrders", method = RequestMethod.GET)
    public AppResponse<List<ColaOrder>> getOrders(String code, boolean isSelf, String state, int page, int size, String type,
                                                  Long startTime, Long endTime, String pairL, String pairR) {

        int start = (page - 1) * size;
        if (size > 500) return new AppResponse<>(ResponseCode.PARAMS_ERROR_CODE, ResponseCode.PARAMS_ERROR_MSG);
        AppResponse<List<ColaOrder>> appResponse = new AppResponse<>();

        if (isSelf) {
            String userId = BaseContextHandler.getUserID();
            if (userId != null) {

                List<ColaOrder> colaOrders = colaBalanceService.searchOrder(userId, code, state, start, size, type, startTime, endTime, pairL, pairR);
                appResponse.setData(colaOrders);
                return appResponse;
            } else return new AppResponse<>(ResponseCode.TOKEN_ERROR_CODE, ResponseCode.TIP_ERROR_MESSAGE);

        } else {

            List<ColaOrder> colaOrders = colaBalanceService.searchOrder(null, code, state, start, size, type, startTime, endTime, pairL, pairR);

            appResponse.setData(colaOrders);
            return appResponse;

        }


    }


    /**
     * 网页 API 上面的接口
     *
     * @param code
     * @param state
     * @param page
     * @param size
     * @param type
     * @param startTime
     * @param endTime
     * @param pairL
     * @param pairR
     * @return com.bitcola.exchange.security.common.msg.TableResultResponse
     * @author zkq
     * @date 2018/10/29 11:50
     */
    @RequestMapping("getSelfOrders")
    public TableResultResponse<ColaOrder> getOrders(String code, String state, int page, int size, String type,
                                                    Long startTime, Long endTime, String pairL, String pairR) {
        String userId = BaseContextHandler.getUserID();
        if (userId == null) {
            return null;
        }
        int start = (page - 1) * size;
        List<ColaOrder> colaOrders = colaBalanceService.searchOrder(userId, code, state, start, size, type, startTime, endTime, pairL, pairR);
        Long total = colaBalanceService.countSelfOrders(userId, code, state, type, startTime, endTime, pairL, pairR);
        return new TableResultResponse<>(total, colaOrders);
    }



    @RequestMapping(value = "getDepth", method = RequestMethod.GET)
    public AppResponse getDepth(String code, double precision, int len, double minCountPrecision) {
        AppResponse appResponse = new AppResponse<DepthData>();

        if (len > 200) return new AppResponse<>(ResponseCode.PARAMS_ERROR_CODE, ResponseCode.PARAMS_ERROR_MSG);
        // var depthData = matchService.coinTraderMap.get(code).getDepth(len,(int)(1/precision));
        //appResponse.setData(depthData);
        return appResponse;

    }


    static boolean checkOrderParams(double price, double count, String type) {
        if (price > 0 && count > 0 && (type.equals("buy") || type.equals("sell")))
            return true;
        return false;

    }


    @RequestMapping("orderManagement")
    public TableResultResponse orderManagement(String code, String state, Integer page, Integer size, String type,
                                               Long startTime, Long endTime, String pairL, String pairR) {
        String userId = BaseContextHandler.getUserID();
        if (page == null || page == 0) {
            page = 1;
        }
        if (size == null || size == 0) {
            size = 10;
        }
        List<Map<String, Object>> list = colaBalanceService.orderManagement(userId, code, state, page, size, type, startTime, endTime, pairL, pairR);
        Long count = colaBalanceService.countOrderManagement(userId, code, state, type, startTime, endTime, pairL, pairR);
        return new TableResultResponse(count, list);
    }

    @RequestMapping("orderHistory")
    public AppResponse orderHistory(String code, Long timestamp, Integer size, String type, Integer isPending) {
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
        List<Map<String, Object>> list = colaBalanceService.orderHistory(userId, timestamp, code, type, size, isPending);
        return new AppResponse(list);
    }

    public static String decimalToString(BigDecimal value) {
        String str = value.toPlainString();
        int len = str.length();
        char[] array = str.toCharArray();
        int index = getDecimalIndex(len, array);
        return str.substring(0, index);
    }

    private static int getDecimalIndex(int len, char[] array) {
        for (int i = 0; i < len; i++) {
            char n = array[i];
            if (n == '.') {
                int j = len;
                while ((array[--j] <= '0') && j >= i) {
                }
                return j + 1;
            }
        }
        return len;
    }

    public static void main(String[] s) {
        System.out.println(new BigDecimal(1D/3D).setScale(2,RoundingMode.HALF_UP));

    }


}
