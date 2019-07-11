package com.bitcola.exchange.caculate.rest;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.exchange.caculate.dataservice.ColaMeService;
import com.bitcola.exchange.caculate.service.ExchangeService;
import com.bitcola.exchange.caculate.service.ExchangeUtils;
import com.bitcola.exchange.security.auth.client.annotation.IgnoreClientToken;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.me.entity.ColaMeBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * @author:wx
 * @description:
 * @create:2018-09-04  00:30
 */
@RequestMapping(value = "balance")
@IgnoreClientToken
@RestController
public class UserBalanceController {


    @Autowired
    ColaMeService colaMeService;
    @Autowired
    ExchangeService exchangeService;
    @Autowired
    ExchangeUtils exchangeUtils;


    @RequestMapping("getBalance")
    //获取用户某个币的余额
    public AppResponse getBalance(String code)
    {
        String userId=BaseContextHandler.getUserID();
        return AppResponse.ok().data(colaMeService.getUserBanlance(userId,code));


    }

    //@RequestMapping("getUSDTAssessment")
    //获取某个币对usdt的估价
    public AppResponse getUSDTAssessment(String code)
    {
        double assessment=exchangeUtils.getCurrentUSDTPrice(code);
        return AppResponse.ok().data(assessment);
    }

    @RequestMapping("getEOSAssessment")
    @Cached(key = " #pairCode",cacheType = CacheType.LOCAL,expire = 10)

    public AppResponse getEOSAssessment(String pairCode)
    {
        double assessment=exchangeUtils.getCurrentEOSPrice(pairCode);
        return AppResponse.ok().data(assessment);
    }


    @RequestMapping("getBTCAssessment")
    @Cached(key = " #pairCode",cacheType = CacheType.LOCAL,expire = 10)

    public AppResponse getBTCAssessment(String pairCode)
    {
        double assessment=exchangeUtils.getCurrentEOSPrice(pairCode);
        return AppResponse.ok().data(assessment);
    }


}
