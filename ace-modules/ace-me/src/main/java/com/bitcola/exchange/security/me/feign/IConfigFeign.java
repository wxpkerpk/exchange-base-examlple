package com.bitcola.exchange.security.me.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "dataservice")
@Service
public interface IConfigFeign {

    @RequestMapping(value = "config/getConfig",method = RequestMethod.GET)
    public String getConfig(@RequestParam("config") String config);


}
