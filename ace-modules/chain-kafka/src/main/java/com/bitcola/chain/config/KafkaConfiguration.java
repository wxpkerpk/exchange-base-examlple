package com.bitcola.chain.config;

import com.bitcola.chain.util.NetUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@Log4j2
public class KafkaConfiguration implements InitializingBean {

    @Value("${bitcola.chain.kafka-topic-receive}")
    public String topic;
    @Value("${bitcola.chain.kafka-group-id}")
    public String groupId;
    @Value("${server.port}")
    public String port;

    @Override
    public void afterPropertiesSet() throws UnknownHostException {
        System.setProperty("topicName", topic);
        if (StringUtils.isNotBlank(groupId)){
            System.setProperty("groupId", groupId);
        } else {
            groupId = NetUtil.getLocalIpAddress()+":"+port;
            System.setProperty("groupId", groupId);
        }
        if (log.isInfoEnabled()){
            log.info("#########  system config topic:{"+topic+"} ########");
            log.info("#########  system config groupId:{"+groupId+"} ########");
        }
    }
}
