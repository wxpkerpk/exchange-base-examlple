package com.bitcola.exchange.bitcolapush.http;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


/**
 * 货币汇率调用示例代码 － 聚合数据
 * 在线接口文档：http://www.juhe.cn/docs/23
 **/

public class ExchangeRate {

    @Scheduled(cron = "0 0 0,12 * * ? ")
    public void refresh(){
        try {
            ExchangeRate.refreshRate();
        }catch (Exception e){

        }
    }

    // 保存当前汇率, 人民币(CNY) 英镑(GBP) 欧元(EUR) 日元(JPY)
    public static BigDecimal CNY = BigDecimal.ZERO;
    public static BigDecimal GBP = BigDecimal.ZERO;
    public static BigDecimal EUR = BigDecimal.ZERO;
    public static BigDecimal JPY = BigDecimal.ZERO;



    public static final String DEF_CHATSET = "UTF-8";
    public static final int DEF_CONN_TIMEOUT = 30000;
    public static final int DEF_READ_TIMEOUT = 30000;
    public static String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";

    //配置您申请的KEY
    public static final String APPKEY = "da9c8883cb42d10b30d0b4eed0158d53"; // 货币汇率
    public static final String HLAPPKEY = "73098c543a06b5a2b9588123395b65e8"; //汇率 key 可以查韩元,后面需要再加

    //1.汇率
    public static void getRequest3() {
        String result = null;
        String url = "http://op.juhe.cn/onebox/exchange/query";//请求接口地址
        Map params = new HashMap();//请求参数
        params.put("key", HLAPPKEY);//APP Key
        params.put("type", "");//两种格式(0或者1,默认为0)

        try {
            result = net(url, params, "GET");
            JSONObject object = JSONObject.parseObject(result);
            if (object.getInteger("error_code") == 0) {
                System.out.println(object.get("result"));
            } else {
                System.out.println(object.get("error_code") + ":" + object.get("reason"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //1.人民币牌价
    public static void getRequest1() {
        String result = null;
        String url = "http://web.juhe.cn:8080/finance/exchange/rmbquot";//请求接口地址
        Map params = new HashMap();//请求参数
        params.put("key", APPKEY);//APP Key
        params.put("type", "");//两种格式(0或者1,默认为0)

        try {
            result = net(url, params, "GET");
            JSONObject object = JSONObject.parseObject(result);
            if (object.getInteger("error_code") == 0) {
                System.out.println(object.get("result"));
            } else {
                System.out.println(object.get("error_code") + ":" + object.get("reason"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //2.外汇汇率
    public static void refreshRate() {
        String result = null;
        String url = "http://web.juhe.cn:8080/finance/exchange/frate";//请求接口地址
        Map params = new HashMap();//请求参数
        params.put("key", APPKEY);//APP Key
        params.put("type", "");//两种格式(0或者1,默认为0)
        try {
            result = net(url, params, "GET");
            JSONObject object = JSONObject.parseObject(result);
            if (object.getInteger("error_code") == 0) {
                JSONArray jsonArray = object.getJSONArray("result");
                if (jsonArray.size()>0){
                    JSONObject list = jsonArray.getJSONObject(0);
                    int i = 1;
                    while(i<20){
                        JSONObject obj = list.getJSONObject("data" + i);
                        if (obj == null){
                            i = 20;
                        }else {
                            String code = obj.getString("code");
                            switch (code) {
                                case "GBPUSD": GBP = BigDecimal.ONE.divide(obj.getBigDecimal("yesPic"),4, RoundingMode.HALF_UP);break;
                                case "EURUSD": EUR = BigDecimal.ONE.divide(obj.getBigDecimal("yesPic"),4, RoundingMode.HALF_UP);break;
                                case "USDCNY": CNY = obj.getBigDecimal("yesPic");break;
                                case "USDJPY": JPY = obj.getBigDecimal("yesPic");break;
                                default:break;
                            }
                            i++;
                        }
                    }
                }
            } else {
                System.out.println(object.get("error_code") + ":" + object.get("reason"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        refreshRate();
    }

    /**
     * @param strUrl 请求地址
     * @param params 请求参数
     * @param method 请求方法
     * @return 网络请求字符串
     * @throws Exception
     */
    public static String net(String strUrl, Map params, String method) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        String rs = null;
        try {
            StringBuffer sb = new StringBuffer();
            if (method == null || method.equals("GET")) {
                strUrl = strUrl + "?" + urlencode(params);
            }
            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            if (method == null || method.equals("GET")) {
                conn.setRequestMethod("GET");
            } else {
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
            }
            conn.setRequestProperty("User-agent", userAgent);
            conn.setUseCaches(false);
            conn.setConnectTimeout(DEF_CONN_TIMEOUT);
            conn.setReadTimeout(DEF_READ_TIMEOUT);
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            if (params != null && method.equals("POST")) {
                try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
                    out.writeBytes(urlencode(params));
                }
            }
            InputStream is = conn.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, DEF_CHATSET));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sb.append(strRead);
            }
            rs = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return rs;
    }

    //将map型转为请求参数型
    public static String urlencode(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry i : data.entrySet()) {
            try {
                sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue() + "", "UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }



}