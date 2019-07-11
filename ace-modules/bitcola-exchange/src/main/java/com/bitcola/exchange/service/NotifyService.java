package com.bitcola.exchange.service;
import com.bitcola.exchange.biz.ColaExchangeBiz;
import com.bitcola.exchange.data.MarketInfo;
import com.bitcola.exchange.message.NotifyMessage;
import com.bitcola.exchange.queue.DelayQueueBySpeed;
import com.bitcola.exchange.queue.DelayQueueBySpeedMap;
import com.bitcola.exchange.util.InFluxDbService;
import com.bitcola.exchange.util.KlineUtil;
import com.bitcola.exchange.websocket.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * @author zkq
 * @create 2019-02-15 10:58
 **/
@Log4j2
@Service
public class NotifyService implements ApplicationRunner, Ordered {


    @Autowired
    WebSocketService webSocketService;

    @Autowired
    InFluxDbService inFluxDbService;

    @Autowired
    ColaExchangeBiz exchangeBiz;

    @Autowired
    KlineService klineService;

    @Resource(name = "personOrderNotifyQueue")
    DelayQueueBySpeed<PersonOrderNotifyMessage> personOrderNotifyQueue;

    @Resource(name = "orderNotifyQueue")
    DelayQueueBySpeedMap<OrderNotifyMessage> orderNotifyQueue;

    @Resource(name = "priceNotifyQueue")
    DelayQueueBySpeedMap<PriceNotifyMessage > priceNotifyQueue;

    @Resource(name = "depthNotifyQueue")
    DelayQueueBySpeedMap<DepthNotifyMessage> depthNotifyQueue;

    @Resource(name = "kLineNotifyQueue")
    DelayQueueBySpeedMap<KlineNotifyMessage> kLineNotifyQueue;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Executors.newFixedThreadPool(1).submit(()->{
            while (true) {
                try {
                    List<PersonOrderNotifyMessage> msg = personOrderNotifyQueue.getMessage();
                    if (msg.size()>0){
                        doPersonOrderNotify(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        for (String pair : MatchService.running.keySet()) {
            startNotifyThread(pair);
        }
        log.info("推送服务 已经启动");
    }

    public void startNotifyThread(String pair) {
        Executors.newFixedThreadPool(1).submit(()->{
            while (true) {
                try {
                    List<OrderNotifyMessage> msg = orderNotifyQueue.getMessage(pair);
                    if (msg.size()>0){
                        doOrderNotify(msg,pair);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Executors.newFixedThreadPool(1).submit(()->{
            while (true) {
                try {
                    List<PriceNotifyMessage> msg = priceNotifyQueue.getMessage(pair);
                    if (msg.size()>0){
                        doPriceNotify(msg,pair);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Executors.newFixedThreadPool(1).submit(()->{
            while (true) {
                try {
                    List<DepthNotifyMessage> msg = depthNotifyQueue.getMessage(pair);
                    if (msg.size()>0){
                        doDepthNotify(msg,pair);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Executors.newFixedThreadPool(1).submit(()->{
            while (true) {
                try {
                    List<KlineNotifyMessage> msg = kLineNotifyQueue.getMessage(pair);
                    if (msg.size()>0){
                        doKlineNotify(msg,pair);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void doPersonOrderNotify(List<PersonOrderNotifyMessage> msg) {
        Map<String, Set<String>> map = new HashMap<>();
        for (PersonOrderNotifyMessage message : msg) {
            String pair = message.getPair();
            Set<String> ids = message.getIds();
            for (String id : ids) {
                Set<String> pairs = map.get(id);
                if (pairs == null){
                    pairs = new HashSet<>();
                }
                pairs.add(pair);
                map.put(id,pairs);
            }
        }
        for (String uid : map.keySet()) {
            webSocketService.sendMessageToTopic(new NotifyMessage(getPersonOrderTopic(uid),map.get(uid)));
        }
    }

    private void doOrderNotify(List<OrderNotifyMessage> messages, String pair) {
        List<OrderNotifyEntity> list = new ArrayList<>();
        for (OrderNotifyMessage message : messages) {
            list.addAll(message.getList());
        }
        List<OrderNotifyEntity> orderNotifyEntities = list.subList(0, list.size() > 30 ? 30 : list.size());
        webSocketService.sendMessageToTopic(new NotifyMessage(getOrderTopic(pair),orderNotifyEntities));
    }

    private void doKlineNotify(List<KlineNotifyMessage> msg,String pair) {
        for (String klineType : KlineUtil.klineTypes.keySet()) {
            Number[] numbers = klineService.getLastKline(pair, klineType);
            if (numbers!=null){
                webSocketService.sendMessageToTopic(new NotifyMessage(getKlineTopic(pair,klineType),numbers));
            }
        }
    }

    private void doPriceNotify(List<PriceNotifyMessage> msg,String pair) {
        MarketInfo pairInfo = klineService.getPairInfo(pair);
        pairInfo.setWorth(exchangeBiz.getPairPrice(pair));
        pairInfo.setPair(pair);
        webSocketService.sendMessageToTopic(new NotifyMessage(getPriceTopic(pair),pairInfo));
    }

    private void doDepthNotify(List<DepthNotifyMessage> messages,String pair) {
        TreeMap<BigDecimal,BigDecimal> treeMapBuy = new TreeMap<>(new Comparator<BigDecimal>() {
            @Override
            public int compare(BigDecimal o1, BigDecimal o2) {
                return o2.compareTo(o1);
            }
        });
        TreeMap<BigDecimal,BigDecimal> treeMapSell = new TreeMap<>(new Comparator<BigDecimal>() {
            @Override
            public int compare(BigDecimal o1, BigDecimal o2) {
                return o1.compareTo(o2);
            }
        });
        for (DepthNotifyMessage message : messages) {
            Map<BigDecimal, BigDecimal> ask = message.getAsk();
            Map<BigDecimal, BigDecimal> bids = message.getBids();
            for (BigDecimal price : ask.keySet()) {
                treeMapSell.put(price,ask.get(price));
            }
            for (BigDecimal price : bids.keySet()) {
                treeMapBuy.put(price,bids.get(price));
            }
        }
        DepthNotifyMessage message = new DepthNotifyMessage();
        message.setAsk(treeMapSell);
        message.setBids(treeMapBuy);
        webSocketService.sendMessageToTopic(new NotifyMessage(getDepthTopic(pair),message.getResult()));
    }

    @Override
    public int getOrder() {
        return 13;
    }

    public String getDepthTopic(String pair){
        return "depth_"+pair;
    }
    public String getPriceTopic(String pair){
        return "price_"+pair;
    }
    public String getKlineTopic(String pair,String klineType){
        return "kline_"+pair+"_"+klineType;
    }
    public String getOrderTopic(String pair){
        return "order_"+pair;
    }
    public String getPersonOrderTopic(String userId){
        return "personOrder_"+userId;
    }


}
