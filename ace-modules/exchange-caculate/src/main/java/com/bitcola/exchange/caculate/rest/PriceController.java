package com.bitcola.exchange.caculate.rest;

/*
 * @author:wx
 * @description:
 * @create:2018-09-08  23:02
 */

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.exchange.caculate.service.ExchangeService;
import com.bitcola.exchange.caculate.service.ExchangeUtils;
import com.bitcola.exchange.caculate.service.InFluxDbService;
import com.bitcola.exchange.security.auth.client.annotation.IgnoreClientToken;
import com.bitcola.exchange.security.common.msg.AppResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "price")
@RestController
@IgnoreClientToken

public class PriceController {
    @Autowired
    ExchangeService exchangeService;


    @Autowired
    ExchangeUtils exchangeUtils;
    @Autowired
    InFluxDbService inFluxDbService;
    @RequestMapping(value = "currentPrice",method = RequestMethod.GET)
    @Cached(key = "#code",cacheType = CacheType.LOCAL,expire = 2)
    public AppResponse currentPrice(String pairCode){




        double price=getCurrentPrice(pairCode);

        return AppResponse.ok().data(price);


    }
    double getCurrentPrice(String code){

        Double priceNow=exchangeUtils.getCurrentPrice(code);
        if(priceNow==null) return 0;
        return priceNow;
    }

    @RequestMapping(value = "getChange",method = RequestMethod.GET)
    @Cached(key = " #pairCode",cacheType = CacheType.LOCAL,expire = 2)

    public AppResponse getChange(String pairCode){

        var data= exchangeUtils.getChange(pairCode);
        return AppResponse.ok().data(data);


    }
    @RequestMapping(value = "maxPrice",method = RequestMethod.GET)
    @Cached(key = " #pairCode",cacheType = CacheType.LOCAL,expire = 2)
    public AppResponse maxPrice(String pairCode){




        double price=inFluxDbService.getMaxIn24h(pairCode);

        return AppResponse.ok().data(price);


    }
    @RequestMapping(value = "minPrice",method = RequestMethod.GET)
    @Cached(key = " #pairCode",cacheType = CacheType.LOCAL,expire = 2)
    public AppResponse minPrice(String pairCode){
        double price=inFluxDbService.getMinIn24h(pairCode);
        return AppResponse.ok().data(price);
    }




}
