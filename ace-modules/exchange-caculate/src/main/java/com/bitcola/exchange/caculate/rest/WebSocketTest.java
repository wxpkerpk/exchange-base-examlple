package com.bitcola.exchange.caculate.rest;

import com.bitcola.exchange.caculate.config.WebSocket;
import com.bitcola.exchange.caculate.service.ExchangeService;
import com.bitcola.exchange.caculate.service.PushDepthService;
import com.bitcola.exchange.caculate.service.PushKlineService;
import com.bitcola.exchange.caculate.service.PushPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wstest")
public class WebSocketTest {
    @Autowired
    WebSocket webSocket;
    @Autowired
    PushPriceService pushPriceService;
    @Autowired
    PushDepthService pushDepthService;
    @Autowired
    PushKlineService pushKlineService;
    @Autowired
    ExchangeService exchangeService;


    @RequestMapping(value = "/sgdsfgsdfgsdfgsegrdfgdsgdsgfs1/dfgdsfgdfgdfg/bbbnnnmmmmm",method = RequestMethod.GET)
    public String reset()
    {

      //
        //  webSocket.sendMessageToTopic(topic,message);
        return "ok";

    }


    @RequestMapping(value = "/add",method = RequestMethod.GET)
    public String add(String pair)
    {
//        pushDepthService.pushDepthMessage(pair);
//        pushKlineService.pushKlineMessage(pair);
//        pushPriceService.pushPriceMessage(pair);

        return "ok";

    }

}
