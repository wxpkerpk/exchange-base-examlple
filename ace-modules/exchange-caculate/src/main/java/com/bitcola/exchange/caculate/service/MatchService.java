package com.bitcola.exchange.caculate.service;

import com.bitcola.exchange.caculate.dataservice.ColaBalanceService;
import com.bitcola.exchange.caculate.dataservice.ColaMeService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * @author:wx
 * @description:自动撮合
 * @create:2018-08-28  16:07
 */
@Service
public class MatchService implements InitializingBean {
    @Autowired
    ColaMeService colaMeService;
    @Autowired
    ExchangeService exchangeService;
    @Autowired
    InFluxDbService inFluxDbService;
    @Autowired
    ExchangeUtils exchangeUtils;
    @Autowired
    ColaBalanceService colaBalanceService;

    @Autowired
    RedisTemplate<Serializable, Object> redisTemplate;

    @Autowired
    PushOrderService pushOrderService;
    @Autowired
    PushKlineService pushKlineService;
    @Autowired
    PushDepthService pushDepthService;
    @Autowired
    PushPriceService pushPriceService;

    //public static Map<String, CoinTrader> coinTraderMap = new HashMap<>();


    public static volatile boolean runMatch = true;

    ExecutorService threadPool = Executors.newFixedThreadPool(24);
    ExecutorService singleTread = Executors.newSingleThreadExecutor();

    void startMatchThread()
    {

        singleTread.submit(()-> {
            List<String> symbols = colaMeService.getAllSymbol();
            for (int i = 0; i < symbols.size(); i++) {
                final String pair = symbols.get(i);
               // CoinTrader coinTrader = new CoinTrader(pair);
                //coinTraderMap.put(pair, coinTrader);
                //exchangeService.initQueueOrders(pair, coinTrader);
                startCancelThread(pair);
                startMatchThread(pair);

            }
        });



    }

    private void startCancelThread(final String pair) {
        new Thread(()->{
            while(true) {
                synchronized (pair) {
                    //var coinTraders = coinTraderMap.get(pair);
                    //while(!coinTraders.cancelQueue().isEmpty()) {
                    //    var cancelOrder= coinTraders.cancelQueue().poll();
                    //    exchangeService.doCancel(cancelOrder);
                   // }


                }
                pause();
            }



        }).start();
    }

    private void pause() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startMatchThread(final String pair) {
        new Thread(() -> {
            while (true) {
                synchronized (pair) {
                    //var coinTraders = coinTraderMap.get(pair);
                    //var matchResult = coinTraders.matchService();
                    //var matchParams = matchResult._1;
                    //if (matchParams.getTransForms().size() == 0) {
                    //    //没有匹配撮合
                    //} else {
                    //    var changeOrder = matchParams.getVisitedOrder();
                    //    var lastPrice = coinTraders.lastPrice();
                    //    var lastSum = coinTraders.lastSum();
                    //    threadPool.submit(() -> {
                    //        inFluxDbService.insertValue(pair, System.currentTimeMillis(), lastPrice, lastSum);
                    //
                    //    });
                    //    int tag = colaBalanceService.matchOrder(matchParams);
                    //    if (tag == 0) {
                    //        coinTraders.setSuccess();//标记为成功
                    //        changeOrder.forEach(order -> {
                    //            pushOrderService.addOrder(order);
                    //            pushOrderService.pushMessage(order.getUserId(), 2000);
                    //        });
                    //        pushDepthService.pushMessage(pair, 1000);
                    //        pushKlineService.pushMessage(pair, 2000);
                    //        pushPriceService.pushMessage(pair, 2000);
                    //    } else {
                    //        //恢复内存订单
                    //        var cacheOrders = matchResult._2;
                    //        cacheOrders.forEach(x -> {
                    //            coinTraders.orderQueue().recoverOrder(x);
                    //        });
                    //
                    //    }

                    //}
                }
                pause();

            }

        }).start();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        startMatchThread();

    }


    @Scheduled(cron = "0/59 * * * * ?")
    void makeKline() {

        List<String> symbols = colaMeService.getAllSymbol();
        for (String symbol : symbols) {

            Double price = exchangeUtils.getCurrentPrice(symbol);
            if (price != null && price > 0)
                inFluxDbService.insertValue(symbol, System.currentTimeMillis(), price, 0);
        }

    }

}
