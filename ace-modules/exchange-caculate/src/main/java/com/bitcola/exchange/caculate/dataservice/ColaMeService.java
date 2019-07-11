package com.bitcola.exchange.caculate.dataservice;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.caculate.entity.DepthData;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.me.entity.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("dataservice")
public interface ColaMeService {
    @RequestMapping("symbol/getCoinSymbolBySymbol")
    @Cached(key = "#symbol")
    AppResponse<List<ColaCoinSymbol>> getCoinSymbolBySymbol(@RequestParam(value = "symbol") String symbol);

    @RequestMapping("symbol/getAllSymbol")
    @Cached(expire = 10,cacheType = CacheType.LOCAL)
    List<String> getAllSymbol();
    @RequestMapping("symbol/getPair")
    @Cached(key = "#pair",expire = 10,cacheType = CacheType.LOCAL)
    ColaCoinSymbol getSymbol(@RequestParam(value = "pair")String pair);
    @RequestMapping("symbol/getDepth")
    DepthData getDepth(@RequestParam(value = "code") String code, @RequestParam(value = "limit") int limit,@RequestParam(value = "precision")double precision,@RequestParam(value = "minCountPrecision") double minCountPrecision
            ,@RequestParam(value = "time") long time
    );

    @RequestMapping("symbol/cancelOrder")
    String cancelOrder(@RequestParam(value = "id") String id);


    @RequestMapping(value = "symbol/list", method = RequestMethod.GET)
    List<ColaUserChooseVo> list(@RequestParam(value = "userId") String userId);

    @RequestMapping(value = "balance/getUserBanlance", method = RequestMethod.GET)
    ColaMeBalance getUserBanlance(@RequestParam(value = "userId") String userId, @RequestParam(value = "code") String code);

    @RequestMapping("symbol/getSymbols")
    @Cached(expire = 5,cacheType = CacheType.LOCAL)
    List<String> getSymbols();

    @RequestMapping(value = "userLimit/getUserLimit",method = RequestMethod.GET)
    @Cached(expire = 6,cacheType = CacheType.LOCAL)
    public ColaUserLimit getUserLimit(@RequestParam("userId")String userId, @RequestParam("module")String module);

}
