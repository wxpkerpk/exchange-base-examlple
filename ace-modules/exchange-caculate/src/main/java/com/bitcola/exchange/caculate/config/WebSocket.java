package com.bitcola.exchange.caculate.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bitcola.exchange.caculate.message.PushMessage;
import com.bitcola.exchange.caculate.service.NotifyDispatchService;
import com.bitcola.exchange.caculate.utils.SpringBeanUtil;
import com.google.common.collect.Maps;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@ServerEndpoint("/ws")
public class WebSocket {

    ExecutorService threadPool = Executors.newFixedThreadPool(12);

    public static Map<Session, List<String>> sessionTopics = new HashMap<>();
    public static Map<String, ConcurrentHashMap<Session, Integer>> topicSession = new HashMap<>();
    static String[] topics = {"kline_1m", "kline_5m", "kline_15m", "kline_30m",
            "kline_1h", "kline_4h", "kline_6h", "kline_8h", "kline_12h", "kline_1d",
            "depth", "priceList"
    };

    @OnOpen
    public void onOpen(Session session) {


    }

    @OnError
    public void onError(Session session, Throwable error) {
        // logger.info("服务端发生了错误"+error.getMessage());
        //error.printStackTrace();
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose(Session session) {

            var topics = sessionTopics.get(session);
            if (topics != null) {
                for (String topic : topics) {
                    topicSession.get(topic).remove(session);
                }
                topics.clear();
            }

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
                        topicSession.put(key, hashSet);
                        sessionSubscribes.add(object.toString());
                        var dispatcher= SpringBeanUtil.getBean(NotifyDispatchService.class);
                        dispatcher.dispatch(key);
                    }
                    sessionTopics.put(session, sessionSubscribes);
                    session.getAsyncRemote().sendText(JSONObject.toJSONString(new PushMessage("subscribeState", "ok")));

                break;
            }

            case "ping": {
                session.getAsyncRemote().sendText("{ping:ok}");
                break;
            }
        }


    }

    public void sendMessageToTopic(String topic, JSONObject message) {
        var sessions = topicSession.get(topic);
        if (sessions != null) {
            for (Session session : sessions.keySet()) {
                synchronized (session) {
                    session.getAsyncRemote().sendText(message.toJSONString());
                }
            }
        }
    }


}
