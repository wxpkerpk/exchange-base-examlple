package com.bitcola.exchange.bitcolapush.util;

import cn.jiguang.common.ClientConfig;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.audience.AudienceTarget;
import cn.jpush.api.push.model.notification.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 极光工具
 *
 * @author zkq
 * @create 2018-11-02 21:20
 **/
@Slf4j
@Component
public class JUtil implements BeanPostProcessor {

    /**
     * 正式 key
     */
    @Value("${jutil.appkey}")
    public String APP_KEY ;
    @Value("${jutil.secret}")
    public String MASTER_SECRET;

    public static JPushClient jpushClient;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        jpushClient = new JPushClient(MASTER_SECRET, APP_KEY, null, ClientConfig.getInstance());
        return bean;
    }




    public static String push(PushPayload payload){
        try {
            PushResult result = jpushClient.sendPush(payload);

        } catch (APIConnectionException e) {
            // Connection error, should retry later
            log.error("Connection error, should retry later", e);

        } catch (APIRequestException e) {
            // Should review the error, and fix the request
            log.error("Should review the error, and fix the request", e);
            log.info("HTTP Status: " + e.getStatus());
            log.info("Error Code: " + e.getErrorCode());
            log.info("Error Message: " + e.getErrorMessage());
        }
        return null;
    }

    /**
     * 给所有人推送
     * @return
     */
    public static PushPayload payloadAllUser(String message) {
        return PushPayload.alertAll(message);
    }

    /**
     * 给某个人推送
     * @return
     */
    public static PushPayload payloadUser(String userId,String message) {
        return PushPayload.newBuilder()
                .setPlatform(Platform.all())
                .setAudience(Audience.alias(userId))
                .setMessage(Message.newBuilder()
                        .setMsgContent(message)
                        .build())
                .build();
    }

    /**
     * 给一些人发送
     * @return
     */
    public static PushPayload payloadUsers(String message,String... users) {
        return PushPayload.newBuilder()
                .setPlatform(Platform.android_ios())
                .setAudience(Audience.newBuilder()
                        .addAudienceTarget(AudienceTarget.alias(users))
                        .build())
                .setMessage(Message.newBuilder()
                        .setMsgContent(message)
                        .addExtra("from", "BitCola")
                        .build())
                .build();
    }
    /**
     * 给一些群组(分组)发送
     * @return
     */
    public static PushPayload payloadGroup(String message,String... group) {
        return PushPayload.newBuilder()
                .setPlatform(Platform.android_ios())
                .setAudience(Audience.newBuilder()
                        .addAudienceTarget(AudienceTarget.tag(group))
                        .build())
                .setMessage(Message.newBuilder()
                        .setMsgContent(message)
                        .addExtra("from", "BitCola")
                        .build())
                .build();
    }

    /**
     * 其他的以后再扩展
     */


}
