package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.me.biz.ColaUserClientConfigBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 客户端配置同步
 * @author zkq
 * @create 2018-10-24 21:22
 **/
@RestController
@RequestMapping("clientConfig")
public class ColaUserClientConfigController {

    @Autowired
    ColaUserClientConfigBiz biz;

    @RequestMapping("get")
    public AppResponse get(String client){
        // !false  !false  !false
        if (!"web".equals(client) && !"android".equals(client) && !"ios".equals(client)){
            return AppResponse.paramsError();
        }
        String config = biz.get(client);
        return AppResponse.ok().data(config);
    }

    @RequestMapping("set")
    public AppResponse set(String client,String config){
        if (!"web".equals(client) && !"android".equals(client) && !"ios".equals(client)){
            return AppResponse.paramsError();
        }
        biz.set(client,config);
        return AppResponse.ok();
    }
}
