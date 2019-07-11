package com.bitcola.exchange.security.me.feign;

import com.bitcola.exchange.security.common.msg.AppResponse;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Repository
@FeignClient(value = "ace-admin")
public interface IUserFeignAdminService {

    @RequestMapping(value = "userFeign/updateLoginPassword",method = RequestMethod.GET)
    public AppResponse updateLoginPassword(@RequestParam("userId") String userId, @RequestParam("username")String username, @RequestParam("newPassword") String newPassword);
}
