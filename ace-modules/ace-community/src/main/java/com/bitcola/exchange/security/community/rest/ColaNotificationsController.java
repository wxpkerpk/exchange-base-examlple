package com.bitcola.exchange.security.community.rest;

import com.bitcola.community.entity.NotificationsVo;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.community.feign.IDataServiceFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-11-05 11:39
 **/
@RestController
@RequestMapping("notifications")
public class ColaNotificationsController {

    @Autowired
    IDataServiceFeign dataServiceFeign;

    @RequestMapping("notReadNumber")
    public AppResponse notReadNumber(){
        Long aLong = dataServiceFeign.notReadNumber(BaseContextHandler.getUserID());
        return AppResponse.ok().data(aLong);
    }

    @RequestMapping("list")
    public AppResponse list(Integer size,Long timestamp){
        if (size == null || size == 0){
            size = 10;
        }
        if (timestamp == null || timestamp == 0){
            timestamp = System.currentTimeMillis();
        }
        List<NotificationsVo> list = dataServiceFeign.list(size, timestamp, BaseContextHandler.getUserID());
        return AppResponse.ok().data(list);
    }

    @RequestMapping(value = "read",method = RequestMethod.POST)
    public AppResponse read(@RequestBody Map<String,String> params){
        String id = params.get("id");
        Integer read = dataServiceFeign.read(id);
        return AppResponse.ok();
    }

    @RequestMapping(value = "readAll",method = RequestMethod.POST)
    public AppResponse readAll(){
        Integer integer = dataServiceFeign.readAll(BaseContextHandler.getUserID());
        return AppResponse.ok().data(integer);
    }

}
