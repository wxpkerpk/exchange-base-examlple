package com.bitcola.exchange.security.common.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 时间
 *
 * @author zkq
 * @create 2018-10-24 15:29
 **/
public class TimeUtils {

    static SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
    static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
    static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private static SimpleDateFormat UTC0 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    /**
     * 当前是项目第几周
     * @param onlineTimestamp 上线日期
     * @return
     */
    public static long getExchangeWeek(long onlineTimestamp){
        long a = System.currentTimeMillis() - onlineTimestamp;
        long week = a / (7 * 24 * 60 * 60 * 1000);
        return week+1;
    }

    /**
     * 当周开始时间
     * @return
     */
    public static long getWeekStartTimestamp(long onlineTimestamp){
        return (getExchangeWeek(onlineTimestamp) - 1) * (7 * 24 * 60 * 60 * 1000) + onlineTimestamp;
    }

    /**
     * 当周结束
     * @return
     */
    public static long getWeekEndTimestamp(long onlineTimestamp){
        return getExchangeWeek(onlineTimestamp) * (7 * 24 * 60 * 60 * 1000) + onlineTimestamp;
    }
    public static Date getDate(String UTC0String) throws Exception{
        UTC0.setTimeZone(TimeZone.getTimeZone("UTC"));
        return UTC0.parse(UTC0String);
    }
    public static String getUTC0String(long timestamp) throws Exception{
        return UTC0.format(new Date(timestamp));
    }

    public static String getFormat(String pattern,long timestamp){
        return new SimpleDateFormat(pattern).format(new Date(timestamp));
    }

    public static String getDateFormat(Long timestamp){
        return dateFormat.format(new Date(timestamp));
    }
    public static String getDateTimeFormat(Long timestamp){
        return dateTimeFormat.format(new Date(timestamp));
    }
    public static String getTimeFormat(Long timestamp){
        return timeFormat.format(new Date(timestamp));
    }
    public static int getYear(Long timestamp){
        return getCalendar(timestamp).get(Calendar.YEAR);
    }
    public static int getMonth(Long timestamp){
        return getCalendar(timestamp).get(Calendar.MONTH)+1;
    }
    public static int getDay(Long timestamp){
        return getCalendar(timestamp).get(Calendar.DAY_OF_MONTH);
    }
    public static int getHour(Long timestamp){
        return getCalendar(timestamp).get(Calendar.HOUR_OF_DAY);
    }
    public static int getMin(Long timestamp){
        return getCalendar(timestamp).get(Calendar.MINUTE);
    }
    public static int getSeccend(Long timestamp){
        return getCalendar(timestamp).get(Calendar.SECOND);
    }
    private static Calendar getCalendar(Long timestamp){
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(timestamp));
        return c;
    }
}
