package com.bitcola.exchange.caculate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class NotifyDispatchService {
    @Autowired
    PushKlineService pushKlineService;

    @Autowired
    PushDepthService pushDepthService;
    @Autowired
    PushOrderService pushOrderService;


    public void dispatch(String topic){

        if(topic.startsWith("depth")){
            String []actor=topic.split("_");
            var pair=actor[1];
            PushDepthService.DepthType depthType=new PushDepthService.DepthType();
            depthType.len= Integer.parseInt(actor[2]);
            depthType.pair=pair;
            depthType.scale= Integer.parseInt(actor[3]);
            var pairTopics=pushDepthService.topic.getOrDefault(pair,new ArrayList<>());
            pairTopics.add(depthType);
            pushDepthService.topic.put(pair,pairTopics);
        }
    }

}
