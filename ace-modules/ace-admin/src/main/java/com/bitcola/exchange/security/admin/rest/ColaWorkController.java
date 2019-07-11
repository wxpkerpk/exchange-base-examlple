package com.bitcola.exchange.security.admin.rest;

import com.bitcola.exchange.security.admin.biz.ColaWorkBiz;
import com.bitcola.exchange.security.common.msg.AppResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 维护工作
 * @author zkq
 * @create 2019-01-31 17:40
 **/
@RestController
@RequestMapping("cola/work")
public class ColaWorkController {

    @Autowired
    ColaWorkBiz biz;

    @RequestMapping("overview")
    public AppResponse overview(){
        Map<String,Object> result = biz.overview();
        return AppResponse.ok().data(result);
    }




}
