package com.bitcola.exchange.util;

import com.bitcola.exchange.data.Kline;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zkq
 * @create 2019-02-25 12:37
 **/
public class KlineUtil {
    public static final String min_1 = "1m";
    public static final String min_5 = "5m";
    public static final String min_15 = "15m";
    public static final String min_30 = "30m";
    public static final String hour_1 = "1h";
    public static final String hour_4 = "4h";
    public static final String hour_6 = "6h";
    public static final String hour_8 = "8h";
    public static final String hour_12 = "12h";
    public static final String day_1 = "1d";
    public static final Map<String,Long> klineTypes = new ConcurrentHashMap<>();
    public static final List<String> types = new ArrayList<>();
    public final Map<String,Kline> kLines = new ConcurrentHashMap<>();
    public BigDecimal yesterdayPrice = BigDecimal.ZERO;
    public BigDecimal yesterdayVol = BigDecimal.ZERO;
    String pair;
    static {
        types.add(min_5);
        types.add(min_15);
        types.add(min_30);
        types.add(hour_1);
        types.add(hour_4);
        types.add(hour_6);
        types.add(hour_8);
        types.add(hour_12);
        types.add(day_1);

        klineTypes.put(min_1,60 * 1000L);
        klineTypes.put(min_5,5 * 60 * 1000L);
        klineTypes.put(min_15,15 * 60 * 1000L);
        klineTypes.put(min_30,30 * 60 * 1000L);
        klineTypes.put(hour_1,60 * 60 * 1000L);
        klineTypes.put(hour_4,4 * 60 * 60 * 1000L);
        klineTypes.put(hour_6,6 * 60 * 60 * 1000L);
        klineTypes.put(hour_8,8 * 60 * 60 * 1000L);
        klineTypes.put(hour_12,12 * 60 * 60 * 1000L);
        klineTypes.put(day_1,24 * 60 * 60 * 1000L);
    }

    public KlineUtil(String pair){
        this.pair = pair;
        kLines.put(min_1,new Kline());
        kLines.put(min_5,new Kline());
        kLines.put(min_15,new Kline());
        kLines.put(min_30,new Kline());
        kLines.put(hour_1,new Kline());
        kLines.put(hour_4,new Kline());
        kLines.put(hour_6,new Kline());
        kLines.put(hour_8,new Kline());
        kLines.put(hour_12,new Kline());
        kLines.put(day_1,new Kline());
    }


    /**
     * 放入 k 线
     * @param price
     * @param number
     * @param timestamp
     */
    public Kline put(BigDecimal price, BigDecimal number, long timestamp){
        Kline isNeedSave = null;
        Kline kline = kLines.get(min_1);
        if (isContainer(timestamp,min_1)){
            if (price.compareTo(kline.getHigh()) > 0) kline.setHigh(price);
            if (price.compareTo(kline.getLow()) < 0) kline.setLow(price);
            kline.setVol(kline.getVol().add(number));
            kline.setClose(price);
        } else {
            isNeedSave = kline; // 需要保存
            kline = new Kline();
            kline.setOpen(price);
            kline.setHigh(price);
            kline.setLow(price);
            kline.setClose(price);
            kline.setVol(number);
            kline.setTime(getTimestampByType(timestamp,min_1));
            kLines.put(min_1,kline);
        }
        for (String type : types) {
            merge(kline,type,number);
        }
        if (isNeedSave != null && isNeedSave.getTime() == 0){
            isNeedSave = kline;
        }
        return isNeedSave;
    }

    /**
     * 将1分钟线合并到其他线
     */
    private void merge(Kline min1,String type,BigDecimal number){
        if (isContainer(min1.getTime(),type)){
            if (min1.getHigh().compareTo(kLines.get(type).getHigh()) > 0 ) kLines.get(type).setHigh(min1.getHigh());
            if (min1.getLow().compareTo(kLines.get(type).getLow()) < 0) kLines.get(type).setLow(min1.getLow());
            kLines.get(type).setVol(kLines.get(type).getVol().add(number));
            kLines.get(type).setClose(min1.getClose());
        } else {
            Kline kline = new Kline();
            kline.setOpen(min1.getOpen());
            kline.setHigh(min1.getHigh());
            kline.setLow(min1.getLow());
            kline.setClose(min1.getClose());
            kline.setVol(min1.getVol());
            kline.setTime(getTimestampByType(min1.getTime(),type));
            if (type.equals(day_1)){
                // 缓存昨日的价格
                Kline yesterday = kLines.get(type);
                if (yesterday.getClose().compareTo(BigDecimal.ZERO) == 0){
                    this.yesterdayPrice = kline.getClose();
                } else {
                    this.yesterdayPrice = yesterday.getClose();
                    this.yesterdayVol = yesterday.getVol();
                }
            }
            kLines.put(type,kline);
        }
    }

    private boolean isContainer(long timestamp,String type){
        return timestamp - kLines.get(type).getTime() < klineTypes.get(type);
    }
    private long getTimestampByType(long timestamp,String type){
        return timestamp - timestamp % klineTypes.get(type);
    }


}
