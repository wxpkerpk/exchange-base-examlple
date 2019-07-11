package com.bitcola.dataservice.controller;

import com.bitcola.dataservice.biz.ColaUserLimitBiz;
import com.bitcola.me.entity.ColaUserLimit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zkq
 * @create 2018-12-24 20:50
 **/
@RestController
@RequestMapping("userLimit")
public class ColaUserLimitController {

    @Autowired
    ColaUserLimitBiz userLimitBiz;

    @RequestMapping("getUserLimit")
    public ColaUserLimit getUserLimit(String userId, String module){
        return userLimitBiz.getUserLimit(userId,module);
    }

}
