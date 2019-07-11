package com.bitcola.exchange.launchpad.feign;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.me.entity.ColaCoinSymbol;
import com.bitcola.me.entity.ColaUserChooseVo;
import com.bitcola.me.entity.ColaUserEntity;
import com.bitcola.me.entity.ColaUserLimit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


/**
 * @author zkq
 * @create 2018-11-08 11:53
 **/
@FeignClient(value = "dataservice")
@Service
public interface IDataServiceFeign {

    @RequestMapping(value = "user/info",method = RequestMethod.GET)
    public ColaUserEntity info(@RequestParam("userId") String userId);

    @RequestMapping(value = "userLimit/getUserLimit",method = RequestMethod.GET)
    public ColaUserLimit getUserLimit(@RequestParam("userId")String userId, @RequestParam("module")String module);

    @RequestMapping("symbol/getPair")
    @Cached(expire = 120,cacheType = CacheType.LOCAL)
    ColaCoinSymbol getSymbol(@RequestParam(value = "pair")String pair);

    @RequestMapping(value = "symbol/list", method = RequestMethod.GET)
    List<ColaUserChooseVo> getUserFavSymbol(@RequestParam(value = "userId") String userId);

    @RequestMapping("symbol/getSymbols")
    @Cached(expire = 5,cacheType = CacheType.LOCAL)
    List<String> getSymbols();
}
