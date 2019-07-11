package com.bitcola.exchange.security.me.biz;

/**
 * 客户端配置同步
 *
 * @author zkq
 * @create 2018-10-24 21:25
 **/

import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.me.mapper.ColaUserClientConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ColaUserClientConfigBiz {
    @Autowired
    ColaUserClientConfigMapper mapper;

    public String get(String client){
        String field = client+"_config";
        return mapper.get(field,BaseContextHandler.getUserID());
    }
    public void set(String client, String config){
        String field = client+"_config";
        mapper.set(field,config,BaseContextHandler.getUserID());
    }
    public void insert(String userId){
        mapper.insert(userId);
    }

}
