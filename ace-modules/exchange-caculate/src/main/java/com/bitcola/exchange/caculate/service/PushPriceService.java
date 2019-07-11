package com.bitcola.exchange.caculate.service;

import com.bitcola.exchange.caculate.data.HomePagePriceLine;

import com.bitcola.exchange.caculate.kafka.KafkaSender;
import com.bitcola.exchange.caculate.message.PushMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class PushPriceService extends PushCustomer {
    ExecutorService cachedThreadPool = Executors.newFixedThreadPool(32);


    @Autowired
    KafkaSender sender;
    @Autowired
    ExchangeUtils exchangeUtils;

    public static String makeTopicStr(String pair)
    {
        return "price_" + pair;
    }

    @Override
    public void action(String pair,Object param) {
        cachedThreadPool.submit(()->{
            System.out.println("计算price开始");

            HomePagePriceLine priceLine=exchangeUtils.getPairDetails(pair);
            sender.send(new PushMessage(makeTopicStr(pair),priceLine));
            System.out.println("计算price结束");

        });

    }




}
