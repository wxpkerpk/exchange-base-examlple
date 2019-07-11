package com.bitcola.exchange.security.auth.controller;

import com.bitcola.exchange.security.auth.biz.SystemBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-11-20 12:59
 **/
@RestController
@RequestMapping("system")
public class SystemController {

    @Autowired
    SystemBiz biz;

    @RequestMapping(value="load",method = RequestMethod.GET)
    public List<Map<String,Object>> load(){
        return biz.load();
    }

}
