package com.bitcola.exchange.security.auth.common.util;

/**
 * Created by wx on 2017/9/10.
 */
public class StringHelper {
    public static String getObjectValue(Object obj){
        return obj==null?"":obj.toString();
    }
}
