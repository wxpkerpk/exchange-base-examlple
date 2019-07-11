package com.bitcola.exchange.bitcolapush.http;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

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
 * 发送邮件
 *
 * @author zkq
 * @create 2018-10-13 15:03
 **/
public class EmailUtils {

    public static final String APP_ID = "13807";
    public static final String APP_KEY = "5d323cb45eb1c3cb30f7e74cbd81a946";
    public static final String EN_EMAIL = "fdudI3";
    public static final String CN_EMAIL = "2tWfU2";

    public static void withdrawSuccessEmail(String email, String coinCode, String number,String realNumber, String language) {

        System.out.println();
    }

    /**
     * 发送邮件验证码
     * @param language 语言 ( ColaLanguage.getCurrentLanguage() )
     * @param to  发给谁
     * @param code  验证码
     * @param antiPhishingCode  钓鱼码,未登录时发送传空串 ""
     */
    public static void sendEmailCaptcha(String language,String to,String code,String antiPhishingCode){
        String URL = "https://api.mysubmail.com/mail/xsend.json";
        HashMap<String, String> paramer = new HashMap<String, String>();
        paramer.put("appid", APP_ID);
        paramer.put("signature", APP_KEY);
        paramer.put("to", to);
        if ("CN".equals(language)){
            paramer.put("project", CN_EMAIL);
        }else {
            paramer.put("project", EN_EMAIL);
        }
        paramer.put("from", "service@mail.bitcola.io");

        JSONObject json = new JSONObject();
        json.put("code", code);
        if (StringUtils.isNotBlank(antiPhishingCode)){
            json.put("antiPhishingCode", antiPhishingCode);
        }else {
            json.put("antiPhishingCode", " ");
        }
        paramer.put("vars", json.toJSONString());
        String result = executePostByUsual(URL, paramer);
        System.out.println("邮件发送给 : "+to + " 验证码 : "+code + " 结果: "+result);
    }


    public static String executePostByUsual(String actionURL, HashMap<String, String> parameters) {
        String response = "";
        try {
            URL url = new URL(actionURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // 发送post请求需要下面两行
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
                requestContent = requestContent + key + "=" + URLEncoder.encode(parameters.get(key), "UTF-8") + "&";
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
