package com.bitcola.exchange.bitcolapush.http;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * 根据IP地址获取详细的地域信息
 *
 */
public class IPUtil {


    // 测试
    public static void main(String[] args) {
        System.out.println(getUserLocation("182.148.57.21"));
    }


    public static String getUserLocation(String userIp){
        if ("0:0:0:0:0:0:0:1".equals(userIp)) return "localhost";
        if ("localhost".equals(userIp)) return "localhost";
        if ("127.0.0.1".equals(userIp)) return "localhost";
        if (userIp.startsWith("192.168.")) return userIp;
        try {
            String url = "http://opendata.baidu.com/api.php?query=" + userIp;
            url += "&co=&resource_id=6006&t=1433920989928&ie=utf8&oe=gbk&format=json";
            String result = sendGet(url);
            JSONObject jsonObject = JSONObject.parseObject(result);
            JSONObject obj = jsonObject.getJSONArray("data").getJSONObject(0);
            String location = obj.getString("location");
            return location;
        }catch (Exception e){
            //e.printStackTrace();
            return "";
        }
    }


    /**
     *
     *
     * @author 张开秋
     * @date 2018/1/10
     * @param url IP地址
     * @return java.lang.String
     */
    private static String sendGet(String url) {
        String result = "";
        BufferedReader in = null;
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            //connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
//                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(),"gbk"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }








}