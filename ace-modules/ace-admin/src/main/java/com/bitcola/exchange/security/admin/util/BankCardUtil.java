package com.bitcola.exchange.security.admin.util;

import com.bitcola.exchange.security.common.util.MD5Utils;

/**
 * @author zkq
 * @create 2019-05-10 17:04
 **/
public class BankCardUtil {

    /**
     * 签名验证
     * @return
     */
    public static boolean checkSign(String sign,String cardId,String username,String userId,String documentNumber){
        return sign.equals(sign(cardId,username,userId,documentNumber));
    }
    public static String sign(String cardId,String username,String userId,String documentNumber){
        return MD5Utils.MD5("cardId:"+cardId+userId+username+documentNumber+"privateKey".toLowerCase());
    }
}
