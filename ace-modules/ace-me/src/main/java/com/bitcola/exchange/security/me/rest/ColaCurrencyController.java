package com.bitcola.exchange.security.me.rest;


import com.alibaba.fastjson.JSONObject;
import com.bitcola.exchange.security.auth.client.annotation.IgnoreUserToken;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.me.feign.IPushFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;

/**
 * 货币汇率
 *
 * @author zkq
 * @create 2018-10-30 17:07
 **/
@RestController
@RequestMapping("currency")
public class ColaCurrencyController {

    @Autowired
    IPushFeign pushFeign;

    /**
     * 汇率
     * @return
     */
    @IgnoreUserToken
    @RequestMapping("rate")
    public AppResponse getExchangeRate(){
        return AppResponse.ok().data(rate());
    }
    /**
     * 汇率
     * @return
     */
    @IgnoreUserToken
    @RequestMapping("rateArray")
    public AppResponse getExchangeRateArray(){
        List<Map> list = new LinkedList<>();
        Map<String, Object> rate = rate();
        for (String key : rate.keySet()) {
            Map item = (Map)rate.get(key);
            item.put("currency",key);
            list.add(item);
        }
        return AppResponse.ok().data(list);
    }

    private Map<String,Object> rate(){
        Map<String,Object> map = new LinkedHashMap<>(5);
        map.put("CNY",0);
        map.put("USD",1);
        map.put("EUR",2);
        map.put("JPY",3);
        map.put("GBP",4);
        Map<String, BigDecimal> currency = pushFeign.currency();
        for (String key : currency.keySet()) {
            String unit = "$";
            switch (key.toUpperCase()){
                case "CNY":unit = "¥";break;
                case "USD":unit = "$";break;
                case "EUR":unit = "€";break;
                case "JPY":unit = "¥";break;
                case "GBP":unit = "£";break;
                default:break;
            }
            BigDecimal price = currency.get(key);
            Map item = new HashMap();
            item.put("price",price);
            item.put("unit",unit);
            map.put(key,item);
        }
        return map;
    }
}
