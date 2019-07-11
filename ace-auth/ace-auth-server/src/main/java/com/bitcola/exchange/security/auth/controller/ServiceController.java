package com.bitcola.exchange.security.auth.controller;

import com.bitcola.exchange.security.auth.biz.ClientBiz;
import com.bitcola.exchange.security.auth.entity.Client;
import com.bitcola.exchange.security.auth.entity.ClientService;
import com.bitcola.exchange.security.common.msg.ObjectRestResponse;
import com.bitcola.exchange.security.common.rest.BaseController;
import org.springframework.web.bind.annotation.*;

/**
 * @author wx
 * @create 2017/12/26.
 */
@RestController
@RequestMapping("service")
public class ServiceController extends BaseController<ClientBiz,Client>{

    @RequestMapping(value = "/{id}/client", method = RequestMethod.PUT)
    @ResponseBody
    public ObjectRestResponse modifyUsers(@PathVariable String id, String clients){
        baseBiz.modifyClientServices(id, clients);
        return new ObjectRestResponse().rel(true);
    }

    @RequestMapping(value = "/{id}/client", method = RequestMethod.GET)
    @ResponseBody
    public ObjectRestResponse<ClientService> getUsers(@PathVariable String id){
        return new ObjectRestResponse<ClientService>().rel(true).data(baseBiz.getClientServices(id));
    }
}
