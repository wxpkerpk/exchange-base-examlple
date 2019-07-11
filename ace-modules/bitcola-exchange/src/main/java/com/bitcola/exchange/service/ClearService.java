package com.bitcola.exchange.service;
import com.bitcola.exchange.constant.NotifyMessageType;
import com.bitcola.exchange.feign.IPushFeign;
import com.bitcola.exchange.mapper.ColaExchangeMapper;
import com.bitcola.exchange.message.MatchMessage;
import com.bitcola.exchange.queue.BitColaBlockingQueueMap;
import com.bitcola.exchange.queue.DelayQueueBySpeed;
import com.bitcola.exchange.queue.DelayQueueBySpeedMap;
import com.bitcola.exchange.websocket.OrderNotifyMessage;
import com.bitcola.exchange.websocket.PersonOrderNotifyMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;


/**
 * 撮合清算
 * @author zkq
 * @create 2019-02-13 10:14
 **/
@Log4j2
@Service
public class ClearService implements ApplicationRunner, Ordered {



    @Autowired
    ClearingHandlerService handlerService;

    @Autowired
    ColaExchangeMapper mapper;

    @Resource(name = "personOrderNotifyQueue")
    DelayQueueBySpeed<PersonOrderNotifyMessage> personOrderNotifyQueue;

    @Resource(name = "orderNotifyQueue")
    DelayQueueBySpeedMap<OrderNotifyMessage> orderNotifyQueue;

    @Resource(name = "clearOrderQueue")
    BitColaBlockingQueueMap<MatchMessage> clearOrderQueue;

    @Autowired
    IPushFeign pushFeign;

    @Value("${spring.redis.host}")
    String ip;

    boolean messageNotify = false;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        cacheInviter();
        for (String pair : MatchService.running.keySet()) {
            startClearThread(pair);
        }
        log.info("清算服务 已经启动");
    }

    public void startClearThread(String pair) {
        Executors.newFixedThreadPool(1).submit(()->{
            while (true) {
                try {
                    List<MatchMessage> message = clearOrderQueue.getMessage(pair);
                    if(message.size()>0){
                        processMatchMessage(message,pair);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!messageNotify && !"120.79.250.164".equals(ip)){
                        messageNotify = true;
                        pushFeign.exchangeError("交易系统中断","18581548780");
                    }
                    return;
                }
            }
        });
    }

    private void cacheInviter() {
        int page = 1;
        int size = 1000;
        while (true){
            List<Map<String,String>> maps = mapper.getInviterAll(page,size);
            for (Map<String, String> map : maps) {
                ClearingHandlerService.inviterMap.put(map.get("u"),map.get("inviter"));
            }
            page++;
            if (maps.size() < size){
                return;
            }
        }
    }


    private void processMatchMessage(List<MatchMessage> message,String pair) {
        Map<String, Object> map = null;

        int maxFailCount=3;

        int count=0;
        while(count<maxFailCount){
            try{
                map = handlerService.batchProcessMatchMessage(message,pair);
                break;
            }catch (Throwable e){
                e.printStackTrace();
                count++;
            }
        }
        if(count==maxFailCount)                    throw new RuntimeException("系统出问题了,尝试了三次都异常");

        PersonOrderNotifyMessage personOrder = (PersonOrderNotifyMessage)map.get(NotifyMessageType.PERSON_ORDER);
        OrderNotifyMessage order = (OrderNotifyMessage)map.get(NotifyMessageType.ORDER);
        personOrderNotifyQueue.putMessage(personOrder);
        if (order.getList().size() > 0){
            orderNotifyQueue.putMessage(pair,order);
        }
    }


    @Override
    public int getOrder() {
        return 11;
    }



}
