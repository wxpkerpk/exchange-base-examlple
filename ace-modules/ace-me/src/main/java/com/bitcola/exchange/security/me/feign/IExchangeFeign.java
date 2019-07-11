package com.bitcola.exchange.security.me.feign;

import com.bitcola.exchange.security.common.msg.AppResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Repository
@FeignClient(value = "bitcola-exchange")
public interface IExchangeFeign {

    @RequestMapping(value = "market",method = RequestMethod.GET)
    AppResponse getChange(@RequestParam("pair") String pair);

    @RequestMapping(value = "getCoinPrice",method = RequestMethod.GET)
    AppResponse getUsdPrice(@RequestParam("coinCode")String coinCode);

    @RequestMapping(value = "admin/addPair",method = RequestMethod.GET)
    boolean addPair(@RequestParam("pair") String pair);

}
