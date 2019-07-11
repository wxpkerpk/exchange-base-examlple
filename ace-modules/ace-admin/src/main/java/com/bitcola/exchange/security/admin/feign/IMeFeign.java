package com.bitcola.exchange.security.admin.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Repository
@FeignClient(value = "ace-me")
public interface IMeFeign {

    @RequestMapping(value = "admin/withdrawPass",method = RequestMethod.GET)
    public String withdrawPass(@RequestParam("orderId") String orderId, @RequestParam("userId")String userId);

}
