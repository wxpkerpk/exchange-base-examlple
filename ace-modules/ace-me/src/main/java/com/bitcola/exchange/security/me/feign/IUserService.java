package com.bitcola.exchange.security.me.feign;

import com.bitcola.exchange.security.common.msg.ObjectRestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Repository
@FeignClient(value = "ace-auth")
public interface IUserService {

    @RequestMapping(value = "jwt/token", method = RequestMethod.POST)
    public ObjectRestResponse<String> createAuthenticationToken(JwtAuthenticationRequest authenticationRequest);

    @RequestMapping(value = "jwt/refresh", method = RequestMethod.GET)
    public ObjectRestResponse<String> refreshToken();

}



