package com.bitcola.exchange.security.admin.rest;

import com.bitcola.exchange.security.admin.feign.IAuthService;
import com.bitcola.exchange.security.admin.feign.JwtAuthenticationRequest;
import com.bitcola.exchange.security.auth.client.annotation.IgnoreClientToken;
import com.bitcola.exchange.security.auth.client.annotation.IgnoreUserToken;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.auth.client.jwt.UserAuthUtil;
import com.bitcola.exchange.security.auth.common.util.jwt.IJWTInfo;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.ObjectRestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author zkq
 * @create 2018-12-11 11:20
 **/
@IgnoreUserToken
@IgnoreClientToken
@RestController
@RequestMapping("login")
public class ColaLoginController {

    @Autowired
    IAuthService authService;
    @Autowired
    UserAuthUtil userAuthUtil;


    @IgnoreClientToken
    @IgnoreUserToken
    @RequestMapping(value = "",method = RequestMethod.POST)
    public AppResponse login(@RequestBody Map<String,String> params) throws Exception{
        String username = params.get("username");
        String password = params.get("password");
        //认证用户 token
        String token;
        ObjectRestResponse<String> authenticationToken = authService.createAuthenticationToken(new JwtAuthenticationRequest(username, password));
        if (authenticationToken.getStatus() == 200){
            token = authenticationToken.getData();
        } else {
            return AppResponse.error(ResponseCode.EX_USER_PASS_INVALID_CODE,ResponseCode.EX_USER_PASS_INVALID_MESSAGE);
        }
        IJWTInfo infoFromToken = userAuthUtil.getInfoFromToken(token);
        Integer id = Integer.valueOf(infoFromToken.getId());
        if (id>100000){
            AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }

        return AppResponse.ok().data(token);
    }


}
