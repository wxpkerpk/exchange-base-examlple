package com.bitcola.exchange.security.admin.rest;

import com.bitcola.exchange.security.admin.biz.ColaConsumerBiz;
import com.bitcola.exchange.security.admin.entity.Consumer;
import com.bitcola.exchange.security.common.msg.AppResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Consumer
 *
 * @author lky
 * @create 2019-04-18 11:39
 **/
@RestController
@RequestMapping("cola/consumer")
public class ColaConsumerController {
    @Autowired
    ColaConsumerBiz biz;

    @RequestMapping(value = "insert", method = RequestMethod.POST)
    public AppResponse insert(@RequestBody Consumer consumer) {
        Consumer c = biz.consumerSelectById(consumer.getId());
        if (c != null) {
            return AppResponse.error("重复添加");
        } else {
            biz.insertConsumer(consumer.getId(), consumer.getDescription());
        }
        return AppResponse.ok();
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public AppResponse delete(@RequestBody Map<String, String> params) {
        biz.deleteConsumer(params.get("id"));
        return AppResponse.ok();
    }

    @RequestMapping(value = "consumerList", method = RequestMethod.GET)
    public AppResponse consumerList() {
        return AppResponse.ok().data(biz.consumerList());
    }

    @RequestMapping(value = "isConsumer", method = RequestMethod.GET)
    public AppResponse isConsumer(@RequestParam("id") String id) {
        return AppResponse.ok().data(biz.consumerSelectById(id)!=null);
    }
}
