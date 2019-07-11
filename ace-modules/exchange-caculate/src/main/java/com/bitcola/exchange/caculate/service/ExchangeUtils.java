package com.bitcola.exchange.caculate.service;

import com.bitcola.caculate.entity.ColaOrder;
import com.bitcola.caculate.entity.DepthData;
import com.bitcola.config.DataServiceConstant;
import com.bitcola.exchange.caculate.config.Constant;
import com.bitcola.exchange.caculate.data.HomePagePriceLine;
import com.bitcola.exchange.caculate.data.TransactionMessage;
import com.bitcola.exchange.caculate.dataservice.ColaBalanceService;
import com.bitcola.exchange.caculate.dataservice.ColaMeService;
import com.bitcola.exchange.caculate.dataservice.PushService;
import com.bitcola.exchange.klock.annotation.Klock;
import io.netty.util.internal.ConcurrentSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.DelayQueue;

import static com.bitcola.exchange.caculate.config.Constant.BTC_CODE;
import static com.bitcola.exchange.caculate.config.Constant.EOS_CODE;
import static com.bitcola.exchange.caculate.config.Constant.USDT_CODE;

/*
 * @author:wx
 * @description:
 * @create:2018-09-15  18:16
 */
@Service
public class ExchangeUtils {
    @Autowired
    @Lazy
    ColaMeService colaMeService;

    @Autowired
    RedisTemplate<Serializable, Object> redisTemplate;

    @Autowired
    @Lazy
    ColaBalanceService colaBalanceService;

    @Autowired
    InFluxDbService inFluxDbService;
    @Autowired
    PushDepthService pushDepthService;


    public static DelayQueue<TransactionMessage> queue = new DelayQueue<>();
    public static Map<String, Boolean> noUpdatePairs = new ConcurrentHashMap<>();


    public void cancelOrder(ColaOrder colaOrder) {


    }













    public double getCurrentUSDTPrice(String code) {

        var currentPrice = getCurrentPrice(code);
        if (currentPrice == 0) return 0;
        var to = code.split("_")[1];
        if (to.equals(USDT_CODE)) return currentPrice;
        var toCode = to + "_" + USDT_CODE;
        var price = getCurrentPrice(toCode);
        if (price == 0) return 0;
        return currentPrice * price;

    }

    @Autowired
    PushService pushService;

    public double getCurrentBTCPrice(String code) {
        return getOtherPrice(code, BTC_CODE, pushService.btcPrice());

    }

    public double getCurrentEOSPrice(String code) {
        return getOtherPrice(code, EOS_CODE, pushService.eosPrice());

    }

    private double getOtherPrice(String code, String otherCode, BigDecimal eosUsdtPrice) {
        var currentPrice = getCurrentPrice(code);
        if (currentPrice == 0) return 0;
        var to = code.split("_")[1];
        if (to.equals(otherCode)) return eosUsdtPrice.multiply(BigDecimal.valueOf(currentPrice)).doubleValue();
        var toCode = to + "_" + EOS_CODE;
        var codeEOSPrice = getCurrentPrice(toCode);

        return eosUsdtPrice.multiply(BigDecimal.valueOf(currentPrice)).multiply(BigDecimal.valueOf(codeEOSPrice)).doubleValue();
    }



    private Double getPriceDetail(Set<Object> colaOrders) {
        if (colaOrders == null || colaOrders.size() == 0) return 0d;
        String colaOrderId = colaOrders.toArray()[0].toString();
        return colaBalanceService.getOrderById(colaOrderId).getPrice().doubleValue();
    }


    private ColaOrder getOrder(Set<Object> colaOrders) {
        if (colaOrders == null || colaOrders.size() == 0) return null;
        String colaOrderId = colaOrders.toArray()[0].toString();
        ColaOrder colaOrder = colaBalanceService.getOrderById(colaOrderId);
        return colaOrder;
    }






    public float getChange(String pair) {

        Double priceBefore24h = inFluxDbService.getPriceBefore24H(pair);
        if (priceBefore24h == 0) return 0;
        Double priceNow = getCurrentPrice(pair);
        if (priceNow == null) return 0f;
        return BigDecimal.valueOf((priceNow - priceBefore24h) / priceBefore24h).setScale(4, RoundingMode.DOWN).floatValue() * 100;

    }

    public double getCurrentPrice(String pair) {

        String key = Constant.PRICE_PRIX + pair;
        Double priceNow = (Double) redisTemplate.opsForValue().get(key);
        if (priceNow == null) return 0d;
        return priceNow;
    }

    public double getVol(String code) {
        Double priceNow = inFluxDbService.getVolIn24h(code);
        return priceNow;
    }

    public BigDecimal getWorth(String symbol, BigDecimal price) {
        if (!"btc".equalsIgnoreCase(symbol)) {
            if ("eth".equalsIgnoreCase(symbol)) {
                price = pushService.ethPrice();
            } else if ("eos".equalsIgnoreCase(symbol)) {
                price = pushService.eosPrice();
            } else if ("usdt".equalsIgnoreCase(symbol)){
                price = BigDecimal.ONE;
            }
        } else {
            price = pushService.btcPrice();
        }
        return price;
    }

    public HomePagePriceLine getPairDetails(String pair) {
        var pairSymbol = colaMeService.getSymbol(pair);
        if(pairSymbol==null) return null;

        HomePagePriceLine pagePriceLine = new HomePagePriceLine(getCurrentPrice(pair), getChange(pair), pair, null, getVol(pair)
                , inFluxDbService.getMaxIn24h(pair), inFluxDbService.getMinIn24h(pair), 0);
        String symbol = pair.split("_")[1];
        BigDecimal price = BigDecimal.ZERO;
        price = getWorth(symbol, price);
        pagePriceLine.setWorth(price.multiply(new BigDecimal(pagePriceLine.getPrice())));
        pagePriceLine.setOpenTime(pairSymbol.getOnlineTime());
        return pagePriceLine;
    }

}
