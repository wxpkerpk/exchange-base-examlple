package com.bitcola.exchange.security.community.rest;

import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.community.biz.ColaLiveBiz;
import com.bitcola.exchange.security.community.constant.LivesConstant;
import com.bitcola.exchange.security.community.entity.LiveEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author lky
 * @create 2019-04-28 15:03
 **/
@RestController
@RequestMapping("/lives")
public class ColaLiveController {
    @Autowired
    ColaLiveBiz colaLiveBiz;


    @RequestMapping(value = "publishLive", method = RequestMethod.POST)
    public AppResponse publishLive(@RequestBody LiveEntity liveEntity) {
        if (StringUtils.isBlank(liveEntity.getContent())  || StringUtils.isBlank(liveEntity.getTitle())||!liveEntity.getType().equals(LivesConstant.TYPE_LIVE)) {
            return AppResponse.paramsError();
        }
        colaLiveBiz.addLive(liveEntity);
        return AppResponse.ok();
    }

    @RequestMapping(value = "changeLive", method = RequestMethod.POST)
    public AppResponse changeLive(@RequestBody LiveEntity liveEntity) {
        if (StringUtils.isBlank(liveEntity.getId())) {
            return AppResponse.paramsError();
        }
        colaLiveBiz.changeLive(liveEntity);
        return AppResponse.ok();
    }

    @RequestMapping(value = "deleteLive", method = RequestMethod.POST)
    public AppResponse deleteLive(@RequestBody Map<String, String> params) {
        String id = params.get("id");
        if (StringUtils.isBlank(id)) {
            return AppResponse.paramsError();
        }
        colaLiveBiz.deleteLive(id);
        return AppResponse.ok();
    }

    @RequestMapping(value = "liveList", method = RequestMethod.GET)
    public AppResponse liveList(String limit, String timestamp) {
        Long time;
        Integer limits;
        if (StringUtils.isBlank(timestamp)) {
            time = System.currentTimeMillis();
        } else {
            time = Long.valueOf(timestamp);
        }
        if (StringUtils.isBlank(limit)) {
            limits = 15;
        } else {
            limits = Integer.valueOf(limit);
        }
        return AppResponse.ok().data(colaLiveBiz.liveList(time, limits));
    }
}
