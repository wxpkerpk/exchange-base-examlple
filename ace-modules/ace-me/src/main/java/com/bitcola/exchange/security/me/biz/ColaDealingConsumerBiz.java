package com.bitcola.exchange.security.me.biz;

import com.bitcola.exchange.security.me.mapper.ColaDealingConsumerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author lky
 * @create 2019-04-18 16:27
 **/
@Service
public class ColaDealingConsumerBiz {
    @Autowired
    ColaDealingConsumerMapper mapper;

    public List<Map<String, String>> consumerList() {
        return mapper.consumerList();
    }
}
