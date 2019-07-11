package com.bitcola.chain.util;

import java.util.regex.Pattern;

/**
 * @author zkq
 * @create 2019-01-25 10:04
 **/
public class MemoUtil {
    private static Pattern prod = Pattern.compile("^\\d{6}$");
    private static Pattern dev = Pattern.compile("^\\d{3}$");

    public static boolean isDevMemo(String memo){
        if (memo == null) return false;
        return dev.matcher(memo).matches();
    }
    public static boolean isProdMemo(String memo){
        if (memo == null) return false;
        return prod.matcher(memo).matches();
    }

}
