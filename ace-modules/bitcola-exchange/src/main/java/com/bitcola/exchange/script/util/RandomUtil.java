package com.bitcola.exchange.script.util;

import com.bitcola.exchange.constant.OrderDirection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author zkq
 * @create 2019-03-20 16:01
 **/
public class RandomUtil {

    /**
     *
     * @param min 最大
     * @param max 最小
     * @param minScale 精度
     * @return
     */
    public static BigDecimal getRandom(BigDecimal min, BigDecimal max, int minScale, int maxScale){
        double minDoubleValue = min.doubleValue();
        double maxDoubleValue = max.doubleValue();
        double random = Math.random() * (maxDoubleValue - minDoubleValue) + minDoubleValue;
        int scale;
        if (minScale == maxScale) {
            scale = minScale;
        } else {
            scale = getRandomInt(minScale,maxScale);
        }
        BigDecimal result = new BigDecimal(random).setScale(scale, RoundingMode.HALF_UP);
        if (result.compareTo(BigDecimal.ZERO) <= 0){
            result = min;
        }
        if (result.compareTo(max) >= 0){
            result = max.subtract(getMinDecimal(maxScale));
        }
        if (result.compareTo(min) <= 0 ){
            result = min.add(getMinDecimal(maxScale));
        }
        if (result.compareTo(max) >= 0) return null;
        return result.setScale(scale,RoundingMode.HALF_UP);
    }

    public static List<String> randomDirections(){
        List<String> list = new ArrayList<>();
        int i = new Random().nextInt(2);
        if (i == 0) {
            list.add(OrderDirection.BUY);
            list.add(OrderDirection.SELL);
        } else {
            list.add(OrderDirection.SELL);
            list.add(OrderDirection.BUY);
        }
        return list;
    }

    public static BigDecimal getMinDecimal(int scale){
        BigDecimal d = BigDecimal.ONE;
        for (int i = 0; i < scale; i++) {
            d = d.multiply(new BigDecimal("0.1"));
        }
        return new BigDecimal(d.toString());
    }

    public static int getRandomInt(int min,int max){
        return new Random().nextInt(1 + max - min) + min;
    }


    private static double cycle = 60 * 1000; // 周期,这个周期内循环一次
    private static double perSecond = 2 * Math.PI / cycle; // 每个周期改增加多少

    /**
     * sin 函数
     * @return
     */
    public static boolean rise(){
        long second = System.currentTimeMillis();
        return Math.sin(second * perSecond) > 0;
    }

    /**
     * 正太分布随机
     * @param sqrt 方差
     * @return
     */
    public static double getZT(double sqrt){
        return Math.sqrt(sqrt) * new Random().nextGaussian();
    }

    public static double getZT(){
        return getZT(1);
    }

    public static String getLastNumber(BigDecimal number){
        String str = number.stripTrailingZeros().toPlainString();
        char[] chars = str.toCharArray();
        for (int i = chars.length -1; i >= 0; i--) {
            if ('0' != chars[i]) return String.valueOf(chars[i]);
        }
        return null;
    }

    public static BigDecimal getZtDecimal(BigDecimal number,double sqrt){
        double zt = getZT(sqrt);
        return number.add(new BigDecimal(zt));
    }


}
