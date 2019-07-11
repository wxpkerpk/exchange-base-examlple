package com.bitcola.exchange.caculate.service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.bitcola.caculate.entity.CaculateParams;
import com.bitcola.caculate.entity.ColaOrder;
import com.bitcola.exchange.caculate.dataservice.ColaBalanceService;
import com.bitcola.exchange.caculate.dataservice.ColaMeService;
import com.bitcola.exchange.klock.annotation.Klock;
import com.bitcola.exchange.klock.annotation.KlockKey;
import com.bitcola.exchange.klock.model.LockType;
import com.bitcola.exchange.security.common.constant.OrderStateConstants;
import com.bitcola.exchange.security.common.msg.ObjectRestResponse;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.bitcola.exchange.security.common.util.DoubleUtinls.*;

/*
 * @author:wx
 * @description:交易服务
 * @create:2018-07-31  19:56
 */
@Service
@Log4j
public class ExchangeService {
    @Autowired
    PushOrderService pushOrderService;
    @Autowired
    PushKlineService pushKlineService;

    @Autowired
    @Lazy
    ColaMeService colaMeService;
    @Autowired
    public RedisTemplate<Serializable, Object> redisTemplate;


    final static double MIN_COUNT = 0;
    @Autowired
    @Lazy
    ColaBalanceService colaBalanceService;

    @Autowired
    InFluxDbService inFluxDbService;
    @Autowired
    ExchangeUtils exchangeUtils;

    @Autowired
    PushPriceService pushPriceService;


    public int makeOrder(String userId, String code, BigDecimal price, BigDecimal count, BigDecimal total, String type) {

        ObjectRestResponse<ColaOrder> response = colaBalanceService.makeOrder(userId, price, code, count, total, type);
        ColaOrder colaOrder = response.getData();
        if (colaOrder != null) {
            //MatchService.coinTraderMap.get(code).putOrder(colaOrder);
        } else
            return -1;
        return 0;
    }


    @Autowired
    PushDepthService pushDepthService;

    public void cancelOrder(String orderId) {
        ColaOrder colaOrder = colaBalanceService.getOrderById(orderId);
        //MatchService.coinTraderMap.get(colaOrder.getCoinCode()).cancelQueue().add(colaOrder);

    }
    public void doCancel(ColaOrder colaOrder){
        exchangeUtils.cancelOrder(colaOrder);
        //MatchService.coinTraderMap.get(colaOrder.getCoinCode()).orderQueue().removeOrder(colaOrder.getId());
        pushDepthService.pushMessage(colaOrder.getCoinCode(), 1000);


    }


    //void initQueueOrders(String code, CoinTrader coinTrader) {
    //
    //    System.out.println(code + "开始启动");
    //
    //
    //    Set<ColaOrder> colaOrderList = new HashSet<>();
    //    int start = 0;
    //    int size = 100;
    //    while (true) {
    //
    //        List<ColaOrder> colaOrders = colaBalanceService.searchOrder(null, code, OrderStateConstants.Pending, start, size, null, 0L, System.currentTimeMillis(), null, null);
    //        if (colaOrders.size() == 0) break;
    //        colaOrderList.addAll(colaOrders);
    //        if (colaOrders.size() < size) {
    //            start += colaOrders.size();
    //        } else {
    //            start += size;
    //        }
    //    }
    //
    //    for (ColaOrder colaOrder : colaOrderList) {
    //        coinTrader.putOrder(colaOrder);
    //    }
    //
    //    System.out.println(code + "启动完成");
    //
    //
    //}

    public static void main(String[] a) {


        double result = 0.00000039 + 84500 / (1e15);
        System.out.println(result);
    }


}
