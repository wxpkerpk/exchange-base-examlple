package com.bitcola.activity.util;


import com.bitcola.exchange.security.common.util.TimeUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zkq
 * @create 2019-05-17 14:18
 **/
public class ColaIsoUtil {
    private static final long stage1 = 1558008000000L;
    private static final Map<Integer,Long> stageStartTimestamp = new HashMap<>();
    private static final Map<Integer,Long> stageEndTimestamp = new HashMap<>();
    static {
        for (int i = 0; i < 10; i++) {
            long stage = stage1 + 72 * 60 * 60 * 1000L * i;
            stageStartTimestamp.put(i+1,stage);
            stageEndTimestamp.put(i+1,stage + 48 * 60 * 60 * 1000L);
        }
    }
    //private static final long stage1 = 1558241400000L;
    //private static final Map<Integer,Long> stageStartTimestamp = new HashMap<>();
    //private static final Map<Integer,Long> stageEndTimestamp = new HashMap<>();
    //static {
    //    for (int i = 0; i < 10; i++) {
    //        long stage = stage1 + 10 * 60 * 1000L * i;
    //        stageStartTimestamp.put(i+1,stage);
    //        stageEndTimestamp.put(i+1,stage + 5 * 60 * 1000L);
    //    }
    //}

    public static long getStartTimestamp(int stage){
        if (stage == 11) return 0;
        return stageStartTimestamp.get(stage);
    }
    public static long getEndTimestamp(int stage){
        if (stage == 11) return 0;
        return stageEndTimestamp.get(stage);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            System.out.println(TimeUtils.getDateTimeFormat(getStartTimestamp(i+1)));
        }
    }

    public static void change(long startTime,long l,long s){
        for (int i = 0; i < 10; i++) {
            long stage = startTime + l * i;
            stageStartTimestamp.put(i+1,stage);
            stageEndTimestamp.put(i+1,stage + s);
        }
    }


}
