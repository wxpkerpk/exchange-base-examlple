package com.bitcola.exchange.security.me.util;

import java.util.TimeZone;

/**
 * 日期处理
 *
 * @author zkq
 * @create 2018-07-15 17:11
 **/
public class DateUtil {

    /**
     * 返回今日零点时间戳
     * @return
     */
    public static long getTodyTime(){
        long current = System.currentTimeMillis();
        long zero = current/(1000*3600*24)*(1000*3600*24) - TimeZone.getDefault().getRawOffset();
        return zero;
    }

}
