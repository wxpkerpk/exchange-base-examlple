package com.bitcola.exchange.script.util;

import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerOutput;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bitcola.exchange.script.data.PlatformPrice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2019-03-19 17:06
 **/
public class PlatformPriceUtil {
    public static final String BINANCE = "https://www.bitcola.app/binance/api/v3/ticker/price";
    public static final String GATEIO = "https://www.bitcola.app/gateio/api2/1/tickers";
    public static final String HUOBI = "https://www.bitcola.app/huobi/market/tickers";
    public static final String BTC = "BTC";
    public static final String ETH = "ETH";
    public static final String USDT = "USDT";


    public static Map<String,BigDecimal> getBinancePrice(){
        String json = OKHttpUtil.httpGet(BINANCE);
        List<JSONObject> list = JSONObject.parseArray(json,JSONObject.class);
        List<PlatformPrice> result = new ArrayList<>();
        for (JSONObject object : list) {
            BigDecimal price = object.getBigDecimal("price");
            String symbol = object.getString("symbol");
            if (symbol.endsWith(BTC)){
                String coinCode = symbol.substring(0, symbol.length() - 3);
                result.add(new PlatformPrice(coinCode+"_"+BTC,price));
            } else if (symbol.endsWith(USDT)){
                String coinCode = symbol.substring(0, symbol.length() - 4);
                result.add(new PlatformPrice(coinCode+"_"+USDT,price));
            } else if (symbol.endsWith(ETH)){
                String coinCode = symbol.substring(0, symbol.length() - 3);
                result.add(new PlatformPrice(coinCode+"_"+ETH,price));
            }
        }
        Map<String,BigDecimal> map = new HashMap<>();
        for (PlatformPrice platformPrice : result) {
            map.put(platformPrice.getPair(),platformPrice.getPrice());
        }
        return map;
    }

    public static Map<String,BigDecimal> getGateioPrice(){
        String json = OKHttpUtil.httpGet(GATEIO);
        JSONObject object = JSONObject.parseObject(json);
        List<PlatformPrice> result = new ArrayList<>();
        for (String pair : object.keySet()) {
            if (pair.endsWith(BTC.toLowerCase()) || pair.endsWith(USDT.toLowerCase()) || pair.endsWith(ETH.toLowerCase())){
                BigDecimal price = object.getJSONObject(pair).getBigDecimal("last");
                result.add(new PlatformPrice(pair.toUpperCase(),price));
            }
        }
        Map<String,BigDecimal> map = new HashMap<>();
        for (PlatformPrice platformPrice : result) {
            map.put(platformPrice.getPair(),platformPrice.getPrice());
        }
        return map;
    }

    public static Map<String,BigDecimal> getHuobiPrice(){
        String json = OKHttpUtil.httpGet(HUOBI);
        JSONObject object = JSONObject.parseObject(json);
        JSONArray data = object.getJSONArray("data");
        List<JSONObject> list = data.toJavaList(JSONObject.class);
        List<PlatformPrice> result = new ArrayList<>();
        for (JSONObject obj : list) {
            BigDecimal price = obj.getBigDecimal("close");
            String symbol = obj.getString("symbol");
            if (symbol.endsWith(BTC.toLowerCase())){
                String coinCode = symbol.substring(0, symbol.length() - 3);
                result.add(new PlatformPrice(coinCode.toUpperCase()+"_"+BTC,price));
            } else if (symbol.endsWith(USDT.toLowerCase())){
                String coinCode = symbol.substring(0, symbol.length() - 4);
                result.add(new PlatformPrice(coinCode.toUpperCase()+"_"+USDT,price));
            } else if (symbol.endsWith(ETH.toLowerCase())){
                String coinCode = symbol.substring(0, symbol.length() - 3);
                result.add(new PlatformPrice(coinCode.toUpperCase()+"_"+ETH,price));
            }
        }
        Map<String,BigDecimal> map = new HashMap<>();
        for (PlatformPrice platformPrice : result) {
            map.put(platformPrice.getPair(),platformPrice.getPrice());
        }
        return map;
    }



}
