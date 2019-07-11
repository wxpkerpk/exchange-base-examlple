package com.bitcola.exchange.launchpad.feign;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.exchange.security.common.msg.AppResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "bitcola-exchange")
@Service
public interface IExchangeFeign {

    @Cached(name = "symbolPrice",cacheType = CacheType.LOCAL,expire = 5)
    @RequestMapping(value = "getCoinPrice",method = RequestMethod.GET)
    AppResponse getUsdPrice(@RequestParam("coinCode")String coinCode);

}
