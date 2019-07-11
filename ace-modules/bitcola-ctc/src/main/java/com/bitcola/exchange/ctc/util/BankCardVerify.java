package com.bitcola.exchange.ctc.util;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.exchange.security.common.util.MD5Utils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author zkq
 * @create 2019-05-08 19:58
 **/
public class BankCardVerify {
    public static final String url = "https://bcard3and4.market.alicloudapi.com/bank3Check";
    public static final String APP_CODE = "83beb67e520243d8973ba53f195fe5ad";

    /**
     * 第三方验证
     * @param bankCardNumber
     * @param idCard
     * @param username
     * @return
     */
    public static String verify(String bankCardNumber,String idCard,String username) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(getUrl(bankCardNumber,idCard,username)).header("Authorization","APPCODE "+APP_CODE).build();
        try {
            Response response = client.newCall(request).execute();
            String result = response.body().string();
            JSONObject json = JSONObject.parseObject(result);
            String status = json.getString("status");
            String cardType = json.getString("cardType");// 借记卡
            if (!status.equals("01")) return json.getString("msg");
            if (!cardType.equals("借记卡")) return "只允许使用借记卡（储蓄卡）";
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "银行账号验证错误";
    }

    private static String getUrl(String bankCardNumber,String idCard,String username){
        return url + "?accountNo="+bankCardNumber+"&idCard="+idCard+"&name="+username;
    }

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
