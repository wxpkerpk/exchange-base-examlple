package com.bitcola.exchange.websocket;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.exchange.biz.ColaExchangeBiz;
import com.bitcola.exchange.config.SpringBeanHandler;
import com.bitcola.exchange.message.NotifyMessage;
import com.bitcola.exchange.rest.ColaExchangeController;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.github.pagehelper.page.PageMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@ServerEndpoint("/webSocket/v2")
public class WebSocketService {


    public static Map<Session, List<String>> sessionTopics = new ConcurrentHashMap<>();
    public static Map<String, ConcurrentHashMap<Session, Integer>> topicSession = new ConcurrentHashMap<>();
    public static final String KLINE = "kline";

    @OnOpen
    public void onOpen(Session session) {
    }

    @OnError
    public void onError(Session session, Throwable error) {
            clearTopic(session);
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose(Session session) {
            clearTopic(session);
    }

    /**
     * 收到客户端的消息
     *
     * @param message 消息
     * @param session 会话
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        JSONObject jsonObject = JSONObject.parseObject(message);

        String type = jsonObject.getString("type");
        if (type == null) type = "subscribe";
        switch (type) {
            case "subscribe": {
                    var subscribes = jsonObject.getJSONArray("subscribe");
                    var sessionSubscribes = sessionTopics.getOrDefault(session, new ArrayList<>());
                    for (String t : sessionSubscribes) {
                        var topicMap = topicSession.get(t);
                        if (t != null) topicMap.remove(session);

                    }
                    sessionSubscribes.clear();
                    for (Object object : subscribes) {
                        String key = object.toString();
                        ConcurrentHashMap<Session, Integer> hashSet = topicSession.getOrDefault(key, new ConcurrentHashMap<>());
                        hashSet.put(session, 1);
                        sessionSubscribes.add(key);
                        if (key.contains(KLINE)){
                            this.sendFirstKline(key,session);
                        }
                        topicSession.put(key, hashSet);
                    }
                    sessionTopics.put(session, sessionSubscribes);
                    session.getAsyncRemote().sendText(JSONObject.toJSONString(new NotifyMessage("subscribeState", "ok")));


                break;
            }

            case "ping": {
                session.getAsyncRemote().sendText(JSONObject.toJSONString(new NotifyMessage("ping", "ok")));
                break;
            }

        }
    }

    public void sendFirstKline(String key, Session session) {
        try {
            String[] split = key.split("_");
            String pair = split[1]+"_"+split[2];
            String klineType = split[3];
            List<Number[]> kline = SpringBeanHandler.getBean(ColaExchangeBiz.class).kline(pair, null, null, 1500, klineType);
            session.setMaxTextMessageBufferSize(100000);
            String jsonString = JSONObject.toJSONString(kline);
            sendByPage(jsonString,key + "_all",session);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendByPage(String sendStr,String topic,Session session) {
        int length = sendStr.length();
        int count = 20000;
        int total = length%count>0?length/count+1:length/count;
        int page = 1;
        for (int i = 0; i < length; i+=count) {
            int subLength = i + count < length ? i + count : length;
            NotifyMessage notifyMessage = new NotifyMessage(topic,sendStr.substring(i,subLength));
            notifyMessage.setT(total);
            notifyMessage.setC(page);
            session.getAsyncRemote().sendText(notifyMessage.toString());
            page++;
        }

    }





    public void sendMessageToTopic(NotifyMessage message) {
        var sessions = topicSession.get(message.getTopic());
        if (sessions != null) {
            for (Session session : sessions.keySet()) {
                try {
                    synchronized (session){
                        if (session.isOpen()){
                            session.getBasicRemote().sendText(message.toString());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void clearTopic(Session session){
        var topics = sessionTopics.get(session);
        if (topics != null) {
            for (String topic : topics) {
                topicSession.get(topic).remove(session);
            }
            topics.clear();
        }
    }


}
