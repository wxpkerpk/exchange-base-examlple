package com.bitcola.exchange.security.community.controller;

import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.community.rest.ColaFeedController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zkq
 * @create 2018-12-25 11:09
 **/
@RestController
@RequestMapping("admin")
public class AdminController {

    @Autowired
    ColaFeedController feedController;

    @RequestMapping("removeItem")
    public boolean removeItem(String id,String type){
        AppResponse appResponse = feedController.deleteItem(type, id,true);
        if (appResponse.getStatus() == 200) return true;
        return false;
    }
}
