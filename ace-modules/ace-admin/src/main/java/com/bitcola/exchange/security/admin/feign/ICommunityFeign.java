package com.bitcola.exchange.security.admin.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "ace-community")
@Repository
public interface ICommunityFeign {

    @RequestMapping(value = "admin/removeItem",method = RequestMethod.GET)
    public boolean removeItem(@RequestParam("id") String id,@RequestParam("type")String type);

}
