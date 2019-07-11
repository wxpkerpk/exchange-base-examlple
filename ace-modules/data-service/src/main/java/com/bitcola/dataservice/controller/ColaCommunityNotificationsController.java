package com.bitcola.dataservice.controller;

import com.bitcola.community.entity.NotificationsEntity;
import com.bitcola.community.entity.NotificationsVo;
import com.bitcola.dataservice.biz.ColaCommunityNotificationsBiz;
import com.bitcola.exchange.security.common.rest.BaseController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zkq
 * @create 2018-11-05 10:52
 **/
@RestController
@RequestMapping("community")
public class ColaCommunityNotificationsController extends BaseController<ColaCommunityNotificationsBiz ,NotificationsEntity> {


    @RequestMapping(value = "add",method = RequestMethod.POST)
    public Integer insert(@RequestBody NotificationsEntity entity){
        return baseBiz.add(entity);
    }

    @RequestMapping("list")
    public List<NotificationsVo> list(Integer size, Long timestamp, String userId){
        return baseBiz.list(size,timestamp,userId);
    }

    @RequestMapping("notReadNumber")
    public Long notReadNumber(String userId){
        return baseBiz.notReadNumber(userId);
    }

    @RequestMapping("read")
    public Integer read(String id){
        return baseBiz.read(id);
    }

    @RequestMapping("readAll")
    public Integer readAll(String userId){
        return baseBiz.readAll(userId);
    }



}
