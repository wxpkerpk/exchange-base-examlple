package com.bitcola.exchange.security.admin.biz;

import com.bitcola.exchange.security.admin.entity.Consumer;
import com.bitcola.exchange.security.admin.mapper.ColaConsumerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lky
 * @create 2019-04-18 11:43
 **/
@Service
public class ColaConsumerBiz {
    @Autowired
    ColaConsumerMapper mapper;

    public void insertConsumer(String id, String description) {
        mapper.insertConsumer(id, description);
    }

    public Consumer consumerSelectById(String id) {
        return mapper.consumerSelectById(id);
    }

    public void deleteConsumer(String id) {
        mapper.deleteConsumer(id);
    }

    public List<Consumer> consumerList() {
        return mapper.consumerList();
    }
}
