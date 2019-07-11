package com.bitcola.exchange.service;
import com.bitcola.exchange.data.Kline;
import com.bitcola.exchange.data.MarketInfo;
import com.bitcola.exchange.message.KLineInsertMessage;
import com.bitcola.exchange.queue.DelayQueueBySpeedMap;
import com.bitcola.exchange.util.InFluxDbService;
import com.bitcola.exchange.util.KlineUtil;
import com.bitcola.exchange.websocket.KlineNotifyMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * @author zkq
 * @create 2019-02-15 09:56
 **/
@Service
@Log4j2
public class KlineService implements ApplicationRunner, Ordered {

    @Autowired
    InFluxDbService inFluxDbService;

    @Resource(name = "kLineNotifyQueue")
    DelayQueueBySpeedMap<KlineNotifyMessage> kLineNotifyQueue;

    @Resource(name = "klineInsertQueue")
    LinkedBlockingQueue<KLineInsertMessage> klineInsertQueue;

    final Map<String,KlineUtil> klineUtils = new ConcurrentHashMap<>();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        for (String pair : MatchService.running.keySet()) {
            startKlineService(pair);
            log.info("初始化: "+pair+" 数据");
            BigDecimal price = inFluxDbService.getMarketPrice(pair);
            BigDecimal vol = inFluxDbService.getVol24H(pair);
            this.doKlineInsertFirst(new KLineInsertMessage(pair,price,vol,System.currentTimeMillis()));
            BigDecimal yesterdayPrice = inFluxDbService.getYesterdayPrice(pair);
            BigDecimal yesterdayVol = inFluxDbService.getYesterdayVol(pair);
            this.setYesterdayPrice(pair,yesterdayPrice,yesterdayVol);
        }
        Executors.newFixedThreadPool(1).submit(()->{
            while (true) {
                try {
                    KLineInsertMessage msg = klineInsertQueue.take();
                    this.doKlineInsert(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        log.info("k线服务启动");
    }

    private void setYesterdayPrice(String pair, BigDecimal yesterdayPrice, BigDecimal yesterdayVol) {
        KlineUtil klineUtil = klineUtils.get(pair);
        klineUtil.yesterdayPrice = yesterdayPrice;
        klineUtil.yesterdayVol = yesterdayVol;
    }

    public void startKlineService(String pair) {
        klineUtils.put(pair,new KlineUtil(pair));
    }

    public void doKlineInsert(KLineInsertMessage msg) {
        KlineUtil klineUtil = klineUtils.get(msg.getPair());
        Kline isNeedSave = klineUtil.put(msg.getPrice(), msg.getNumber(), msg.getTimestamp());
        if (isNeedSave != null){
            inFluxDbService.insertValue(msg.getPair(),isNeedSave);
        }
    }

    public void doKlineInsertFirst(KLineInsertMessage msg) {
        KlineUtil klineUtil = klineUtils.get(msg.getPair());
        Kline isNeedSave = klineUtil.put(msg.getPrice(), BigDecimal.ZERO, msg.getTimestamp());
        if (isNeedSave != null){
            inFluxDbService.insertValue(msg.getPair(),isNeedSave);
        }
        klineUtil.kLines.get(KlineUtil.day_1).setVol(msg.getNumber());
    }


    public Number[] getLastKline(String pair,String klineType){
        KlineUtil klineUtil = klineUtils.get(pair);
        if (klineUtil == null) return null;
        Kline kline = klineUtil.kLines.get(klineType);
        return kline.toArray();
    }


    public MarketInfo getPairInfo(String pair){
        KlineUtil klineUtil = klineUtils.get(pair);
        Kline kline = klineUtil.kLines.get(KlineUtil.day_1);
        BigDecimal price = kline.getClose();
        BigDecimal max = kline.getHigh();
        BigDecimal min = kline.getLow();
        BigDecimal vol = kline.getVol().add(klineUtil.yesterdayVol);// 成交量改为48小时
        BigDecimal change = this.getGain24(klineUtil.yesterdayPrice,price);

        MarketInfo marketInfo = new MarketInfo();
        marketInfo.setPrice(price);
        marketInfo.setMax_24h(max);
        marketInfo.setMin_24h(min);
        marketInfo.setVol(vol);
        marketInfo.setGain_24(change);
        return marketInfo;
    }

    private BigDecimal getGain24(BigDecimal before24H,BigDecimal marketPrice){
        if (marketPrice == null || marketPrice.compareTo(BigDecimal.ZERO)==0){
            return BigDecimal.ZERO;
        }
        if (before24H == null || before24H.compareTo(BigDecimal.ZERO)==0){
            return BigDecimal.ONE;
        }
        return marketPrice.subtract(before24H).divide(before24H,4, RoundingMode.HALF_UP);
    }

    public BigDecimal getMarketPrice(String pair){
        KlineUtil klineUtil = klineUtils.get(pair);
        if (klineUtil == null) return BigDecimal.ZERO;
        Kline kline = klineUtil.kLines.get(KlineUtil.day_1);
        return kline.getClose();
    }



    @Override
    public int getOrder() {
        return 12;
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    public void task(){
        long currentTimeMillis = System.currentTimeMillis();
        for (String pair : MatchService.running.keySet()) {
            BigDecimal marketPrice = this.getMarketPrice(pair);
            if (marketPrice.compareTo(BigDecimal.ZERO) != 0){
                this.doKlineInsert(new KLineInsertMessage(pair,marketPrice,BigDecimal.ZERO,currentTimeMillis));
            }
        }
    }
    @Scheduled(cron = "30 0/1 * * * ?")
    public void notifyTask1m(){
        for (String pair : MatchService.running.keySet()) {
            kLineNotifyQueue.putMessage(pair,new KlineNotifyMessage());
        }
    }





}
