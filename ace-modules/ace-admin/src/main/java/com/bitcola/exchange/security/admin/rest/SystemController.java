package com.bitcola.exchange.security.admin.rest;

import com.bitcola.exchange.security.admin.biz.ColaSystemBiz;
import com.bitcola.exchange.security.common.msg.AppResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-11-29 10:31
 **/
@RestController
@RequestMapping("cola/system")
public class SystemController {

    @Autowired
    ColaSystemBiz biz;

    @RequestMapping("status")
    public AppResponse status(){
        List<Map<String,Object>> result = biz.status();
        return AppResponse.ok().data(result);
    }

    @RequestMapping(value = "maintain",method = RequestMethod.POST)
    public AppResponse maintain(@RequestBody Map<String,Object> params){
        String module = params.get("module").toString();
        String timestamp = params.get("timestamp").toString();
        biz.maintain(module,timestamp);
        return AppResponse.ok();
    }

    @RequestMapping(value = "statusSuccess",method = RequestMethod.POST)
    public AppResponse statusSuccess(@RequestBody Map<String,Object> params){
        String module = params.get("module").toString();
        biz.statusSuccess(module);
        return AppResponse.ok();
    }



}
