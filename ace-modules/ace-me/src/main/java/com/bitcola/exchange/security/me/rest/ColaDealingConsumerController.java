package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.me.biz.ColaDealingConsumerBiz;
import com.bitcola.exchange.security.me.feign.IChatFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author lky
 * @create 2019-04-18 16:31
 **/
@RestController
@RequestMapping("cola/dealingConsumer")
public class ColaDealingConsumerController {
    @Autowired
    ColaDealingConsumerBiz biz;
    @Autowired
    IChatFeign chatFeign;

    @RequestMapping(value = "getConsumer", method = RequestMethod.GET)
    public AppResponse consumerList() {
        List<Map<String, String>> consumerList = biz.consumerList();
        if (consumerList.size() != 0) {
            consumerList.removeIf(consumer -> !chatFeign.isOnline(consumer.get("id")));
            if (consumerList.size() != 0) {
                return getResponse(consumerList);
            } else {
                return getResponse(biz.consumerList());
            }
        } else {
            return AppResponse.error("没有客服");
        }
    }


    public AppResponse getResponse(List<Map<String, String>> list) {
        int random = new Random().nextInt(list.size());
        int defaultNum = new Random().nextInt(3);
        String defaultContent = "";
        if (defaultNum == 0) {
            defaultContent = ColaLanguage.get(ColaLanguage.CONSUMER_DEFAULT_ONE);
        } else if (defaultNum == 1) {
            defaultContent = ColaLanguage.get(ColaLanguage.CONSUMER_DEFAULT_TWO);
        } else if (defaultNum == 2) {
            defaultContent = ColaLanguage.get(ColaLanguage.CONSUMER_DEFAULT_THREE);
        }
        chatFeign.sendChatMessage(list.get(random).get("id"), BaseContextHandler.getUserID(), "我需要帮助");
        chatFeign.sendChatMessage(BaseContextHandler.getUserID(), list.get(random).get("id"), defaultContent);

        return AppResponse.ok().data(list.get(random));
    }
}
