package com.bitcola.exchange.launchpad.feign;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "dataservice")
@Service
public interface IConfigFeign {

    @RequestMapping(value = "config/getConfig",method = RequestMethod.GET)
    @Cached(cacheType = CacheType.LOCAL, expire = 30)
    public String getConfig(@RequestParam("config") String config);


}
