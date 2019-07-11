package com.bitcola.exchange.caculate.dataservice;


import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.math.BigDecimal;

@FeignClient("bitcola-push")
public interface PushService {
    @RequestMapping(value = "/price/eos",method = RequestMethod.GET)

    @Cached(expire = 20,cacheType = CacheType.LOCAL)
    BigDecimal eosPrice();

    @RequestMapping(value = "/price/btc",method = RequestMethod.GET)
    @Cached(expire = 20,cacheType = CacheType.LOCAL)
    BigDecimal btcPrice();

    @RequestMapping(value = "/price/eth",method = RequestMethod.GET)
    @Cached(expire = 20,cacheType = CacheType.LOCAL)
    BigDecimal ethPrice();
}
