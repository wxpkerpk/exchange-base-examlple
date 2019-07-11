package com.bitcola.exchange.security.admin.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author zkq
 * @create 2018-11-14 10:12
 **/
@FeignClient(value = "bitcola-exchange")
@Repository
public interface IExchangeFeign {

    @RequestMapping(value = "feign/cancelOrder",method = RequestMethod.GET)
    public boolean cancelOrder(@RequestParam("orderId") String orderId,@RequestParam("key") String key);

}
