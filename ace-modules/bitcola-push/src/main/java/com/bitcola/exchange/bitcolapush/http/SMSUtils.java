package com.bitcola.exchange.bitcolapush.http;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Set;

/**
 * 短信工具
 *
 * @create 2018-09-26 16:57
 **/
public class SMSUtils {
    /**
     * 中国区号
     */
    public static final String CHINA_CODE = "86";
    private static final String APP_ID = "27865";
    private static final String APP_KEY = "084cb4d4684a516788aa7812529dc7a4";
    private static final String INTERNATIONAL_APP_ID = "60401";
    private static final String INTERNATIONAL_APP_KEY = "59f50ba8c0287b9e3f3fe1e6a9629377";

    private static final String CONTENT_CAPTCHA_CN = "【BITCOLA】您好！感谢您选择BitCola！您的验证码是：%s（10分钟内有效）。请勿转发或告知他人。";
    private static final String CONTENT_CAPTCHA_EN = " Thank you for choosing BitCola! Your verification code is: %s (valid for 10 minutes). Do not forward or inform others.";

    /**
     * 发送短信验证码
     * @param areaCode 国际区号 不带+ 比如 "86"
     * @param mobile 手机号
     * @param captcha 验证码
     */
    public static void sendCaptchaSMS(String areaCode,String mobile,String captcha){
        String response;
        if (CHINA_CODE.equals(areaCode)){
            response = SMSUtils.sendChinaSMS(mobile, String.format(CONTENT_CAPTCHA_CN, captcha));
        } else {
            response = SMSUtils.sendInternational("+"+areaCode+mobile, String.format(CONTENT_CAPTCHA_EN,captcha));
        }
        System.out.println("短信发送: "+mobile+"  "+response);
    }


    public static void sendSMS(String areaCode,String telephone,String content){
        String response;
        if (CHINA_CODE.equals(areaCode)){
            response = SMSUtils.sendChinaSMS(telephone, content);
        } else {
            response = SMSUtils.sendInternational("+"+areaCode+telephone, content);
        }
        System.out.println("短信发送: "+telephone+"  "+response);
    }

    public static String sendChinaSMS(String to, String content) {
        String URL = "http://api.mysubmail.com/message/send.json";
        HashMap<String, String> paramer = new HashMap<String, String>();
        paramer.put("appid", APP_ID);
        paramer.put("signature", APP_KEY);
        paramer.put("to", to);
        paramer.put("content", content);
        return executePostByUsual(URL, paramer);
    }

    public static String sendInternational( String to, String content) {
        String URL = "https://api.mysubmail.com/internationalsms/send.json";
        HashMap<String, String> paramer = new HashMap<String, String>();
        paramer.put("appid", INTERNATIONAL_APP_ID);
        paramer.put("signature", INTERNATIONAL_APP_KEY);
        paramer.put("to", to);
        paramer.put("content", content);
        return executePostByUsual(URL, paramer);
    }

    public static String executePostByUsual(String actionURL, HashMap<String, String> parameters) {
        String response = "";
        try {
            URL url = new URL(actionURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // 设置请求数据内容
            String requestContent = "";
            Set<String> keys = parameters.keySet();
            for (String key : keys) {
                requestContent = requestContent + URLEncoder.encode(key) + "=" + URLEncoder.encode(parameters.get(key))
                        + "&";
            }
            requestContent = requestContent.substring(0, requestContent.lastIndexOf("&"));
            DataOutputStream ds = new DataOutputStream(connection.getOutputStream());
            // 使用write(requestContent.getBytes())是为了防止中文出现乱码
            ds.write(requestContent.getBytes());
            ds.flush();
            try {
                // 获取URL的响应
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                String s = "";
                String temp = "";
                while ((temp = reader.readLine()) != null) {
                    s += temp;
                }
                response = s;
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("No response get!!!");
            }
            ds.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Request failed!");
        }
        return response;
    }




}
