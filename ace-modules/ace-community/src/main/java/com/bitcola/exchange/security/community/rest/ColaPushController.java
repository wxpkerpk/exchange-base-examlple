package com.bitcola.exchange.security.community.rest;

import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.community.feign.IPushFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zkq
 * @create 2018-11-05 16:07
 **/
@RestController
@RequestMapping("push")
public class ColaPushController {

    @Autowired
    IPushFeign pushFeign;


    @RequestMapping("pushOne")
    public AppResponse pushOne(String message,String userId){
        pushFeign.one(message,userId);
        return AppResponse.ok();
    }

}
