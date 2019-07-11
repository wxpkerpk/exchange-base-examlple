package com.bitcola.exchange.security.admin.controller;

import com.bitcola.exchange.security.admin.biz.UserBiz;
import com.bitcola.exchange.security.admin.entity.User;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.msg.AppResponse;
import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zkq
create 2018-10-10 17:21
 **/
@RequestMapping("userFeign")
@RestController
public class UserFeignController {

    @Autowired
    UserBiz userBiz;

    @RequestMapping(value = "updateLoginPassword",method = RequestMethod.GET)
    public AppResponse updateLoginPassword(@RequestParam("userId") String userId, @RequestParam("username")String username, @RequestParam("newPassword") String newPassword){
        try {
            User user = new User();
            user.setId(userId);
            user.setPassword(newPassword);
            user.setUsername(username);
            userBiz.updateSelectiveById(user);
        } catch (Exception e){
            e.printStackTrace();
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        return AppResponse.ok();
    }
}
