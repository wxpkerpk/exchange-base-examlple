package com.bitcola.exchange.ctc.util;

import com.alibaba.fastjson.JSONObject;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author zkq
 * @create 2019-05-08 14:44
 **/
@Log4j2
@Component
public class UsdtPriceUtil {

    BigDecimal price = BigDecimal.ZERO;
    BigDecimal usdtPrice = new BigDecimal("6.8");
    BigDecimal rate = new BigDecimal("0.2");
    BigDecimal priceSub = new BigDecimal("0.01");

    @Cached(cacheType = CacheType.LOCAL,expire = 60)
    public BigDecimal getPrice(){
        try {
            String json = OKHttpUtil.httpGet("https://www.bitcola.app/gateio/api2/1/orderBook_c2c/usdt_cny");
            JSONObject otcPrice = JSONObject.parseObject(json);
            if (otcPrice.getBooleanValue("result")){
                price = otcPrice.getJSONArray("asks").getJSONArray(0).getBigDecimal(0);
            } else {
                log.error("请求 USDT 价格失败,使用上次的价格");
            }
        } catch (Exception e) {
            log.error("请求 USDT 价格失败,使用上次的价格");
        }
        if (usdtPrice.subtract(price).abs().divide(usdtPrice,4, RoundingMode.HALF_UP).compareTo(rate)>0){
            throw new RuntimeException();
        }
        return price;
    }

    public BigDecimal getSellPrice(){
        return getPrice().subtract(priceSub);
    }


}
