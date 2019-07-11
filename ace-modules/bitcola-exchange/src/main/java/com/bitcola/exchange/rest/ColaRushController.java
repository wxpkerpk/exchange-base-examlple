package com.bitcola.exchange.rest;

import com.bitcola.exchange.data.RushParams;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.service.RushService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author zkq
 * @create 2019-04-22 14:38
 **/
@RestController
@RequestMapping("rush")
public class ColaRushController {
    @Autowired
    RushService rushService;

    @RequestMapping(method = RequestMethod.POST,value = "start")
    public AppResponse start(@RequestBody RushParams params){
        if (StringUtils.isAnyBlank(params.getRushPair(),params.getRushProjectUserId())){
            return AppResponse.paramsError();
        }
        if (params.getRushPrice().size()==0 || params.getRushTimestampStart().size() == 0 ||
                params.getRushTimestampEnd().size() == 0){
            return AppResponse.paramsError();
        }
        rushService.init(params);
        return AppResponse.ok();
    }

    @RequestMapping(method = RequestMethod.POST,value = "stop")
    public AppResponse stop(){
        rushService.stop();
        return AppResponse.ok();
    }

    @RequestMapping(method = RequestMethod.GET,value = "status")
    public AppResponse status(){
        return AppResponse.ok().data(rushService.getParams());
    }

}
