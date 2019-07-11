package com.bitcola.exchange.caculate.rest;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.exchange.caculate.data.HomePagePriceLine;
import com.bitcola.exchange.caculate.dataservice.ColaBalanceService;
import com.bitcola.exchange.caculate.dataservice.ColaMeService;
import com.bitcola.exchange.caculate.dataservice.PushService;
import com.bitcola.exchange.caculate.service.ExchangeService;
import com.bitcola.exchange.caculate.service.ExchangeUtils;
import com.bitcola.exchange.caculate.service.InFluxDbService;
import com.bitcola.exchange.security.auth.client.annotation.IgnoreClientToken;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.me.entity.ColaCoinSymbol;
import com.bitcola.me.entity.ColaCoinUserchoose;
import com.bitcola.me.entity.ColaUserChooseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/*
 * @author:wx
 * @description:
 * @create:2018-09-24  15:44
 */
@RequestMapping(value = "favorites")
@RestController
@IgnoreClientToken

public class UserFavoritesController {
    @Autowired
    ExchangeService exchangeService;
    @Autowired
    @Lazy
    ColaBalanceService colaBalanceService;
    @Autowired
    InFluxDbService inFluxDbService;

    @Autowired
    @Lazy
    ColaMeService colaMeService;


    @Autowired
    @Lazy
    ExchangeUtils exchangeUtils;
    @Autowired
    PushService pushService;

    //获取自选
    @RequestMapping("list")
    public AppResponse<List<HomePagePriceLine>> list() {

        List<HomePagePriceLine> homePagePriceLines = new ArrayList<>();

        List<ColaUserChooseVo> colaCoinUserchooses=  colaMeService.list(BaseContextHandler.getUserID());

        for(ColaUserChooseVo colaCoinUserchoose:colaCoinUserchooses){
            String pair=colaCoinUserchoose.getPair();
            var symbolPair=colaMeService.getSymbol(pair);
            if(symbolPair==null) continue;
            HomePagePriceLine pagePriceLine = new HomePagePriceLine(exchangeUtils.getCurrentPrice(pair), exchangeUtils.getChange(pair), pair, colaCoinUserchoose.getIcon(), exchangeUtils.getVol(pair)
                    , inFluxDbService.getMaxIn24h(pair), inFluxDbService.getMinIn24h(pair), colaCoinUserchoose.getSort()
            );            homePagePriceLines.add(pagePriceLine);
            BigDecimal price = BigDecimal.ZERO;
            String symbol = pair.split("_")[1];

            price = exchangeUtils.getWorth(symbol, price);
            pagePriceLine.setOpenTime(symbolPair.getOnlineTime());
            pagePriceLine.setWorth(price.multiply(new BigDecimal(pagePriceLine.getPrice())));
        }
        AppResponse resp = new AppResponse<>();
        resp.setData(homePagePriceLines);
        return resp;
    }
}
