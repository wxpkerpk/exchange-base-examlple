package com.bitcola.exchange.bitcolapush.controller;

import com.bitcola.exchange.bitcolapush.util.JUtil;
import com.bitcola.exchange.security.common.msg.AppResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 推送
 *
 * @author zkq
 * @create 2018-11-02 21:20
 **/
@RestController
@RequestMapping("push")
public class BitColaPushController {


    @RequestMapping("all")
    public AppResponse all(String message){
        JUtil.push(JUtil.payloadAllUser(message));
        return AppResponse.ok();
    }

    @RequestMapping("one")
    public AppResponse one(String message,String userId){
        JUtil.push(JUtil.payloadUser(userId,message));
        return AppResponse.ok();
    }


}
