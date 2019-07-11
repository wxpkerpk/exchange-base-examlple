package com.bitcola.dataservice.controller;

import com.bitcola.dataservice.biz.ColaConfigBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 配置
 *
 * @author zkq
 * @create 2018-10-18 15:09
 **/
@RestController
@RequestMapping("config")
public class ColaConfigController {

    @Autowired
    ColaConfigBiz colaConfigBiz;


    @RequestMapping(value = "getConfig",method = RequestMethod.GET)
    public String getConfig(@RequestParam("config") String config){
        return colaConfigBiz.getConfig(config);
    }
}
