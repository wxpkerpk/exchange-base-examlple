package com.bitcola.exchange.caculate.service;


import com.bitcola.exchange.caculate.kafka.KafkaSender;
import com.bitcola.exchange.caculate.message.PushMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class PushKlineService extends PushCustomer {

    ExecutorService cachedThreadPool = Executors.newFixedThreadPool(32);

    @Autowired
    KafkaSender kafkaSender;
    @Autowired
    InFluxDbService inFluxDbService;
    public final static int s=1000;//秒
    public final static int m=1000*60;//分
    public final static int h=1000*60*60;//时
    public final static int d=1000*60*60*24;//天

    public static String[] klineTypes={"1m","5m","15m","30m"};
    public  String makeTopicStr(String pair,String type)
    {
        StringBuilder stringBuilder=new StringBuilder(12);
        stringBuilder.append("kline_").append(pair).append("_").append(type);
        return stringBuilder.toString().intern() ;
    }

    @Override
    public void action(String pair,Object param) {
        cachedThreadPool.submit(()->{
            System.out.println("计算推送k线开始");
            for (int i = 0; i <klineTypes.length ; i++) {
                Number[]kline=inFluxDbService.queryLastKline(pair,klineTypes[i]);
                kafkaSender.send(new PushMessage(makeTopicStr(pair,klineTypes[i]),kline));
            }
            System.out.println("计算推送k线结束");

        });

    }





}
