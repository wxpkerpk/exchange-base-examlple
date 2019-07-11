package com.bitcola.exchange.caculate.service;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.caculate.entity.ColaOrder;
import com.bitcola.exchange.caculate.dataservice.ColaBalanceService;
import com.bitcola.exchange.caculate.kafka.KafkaSender;
import com.bitcola.exchange.caculate.message.PushMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class PushOrderService extends PushCustomer {
    @Autowired
    KafkaSender kafkaSender;
    Map<String,Map<String, ConcurrentLinkedQueue<ColaOrder>>>pairIds=new HashMap<>();

    public void addOrder(ColaOrder colaOrder) {
        var pair=colaOrder.getCoinCode();
       var idMap=  pairIds.getOrDefault(colaOrder.getUserId(), new HashMap<>());
       var ids=
               idMap.getOrDefault(pair,new ConcurrentLinkedQueue<>());

       ids.offer(colaOrder);
       idMap.put(pair,ids);
       pairIds.put(colaOrder.getUserId(),idMap);
    }
    @Override
    public void action(String userId, Object param) {
         if(param instanceof Map) {
              var map= (Map<String,ConcurrentLinkedQueue<ColaOrder>>)param;
              Map<String,List<ColaOrder>>userContent =new HashMap<>();
              map.forEach((key,value)->{
                  var colaOrders=new ArrayList<ColaOrder>();
                  while(!value.isEmpty()) {
                   ColaOrder order=   value.poll();
                   colaOrders.add(order);
                  }
                  userContent.put(key,colaOrders);
              });
              kafkaSender.send(new PushMessage(makeTopicStr(userId), userContent));
         }

    }

    static String makeTopicStr(String userId) {
        return "orderChange_" + userId;
    }

    @Override
    public Object getActionParams(String userId) {
        return pairIds.get(userId);
    }
}
