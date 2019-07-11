package com.bitcola.exchange.security.auth.biz;

import com.bitcola.exchange.security.auth.mapper.SystemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-11-20 14:58
 **/
@Service
public class SystemBiz {

    @Autowired
    SystemMapper mapper;

    public List<Map<String, Object>> load() {
        return mapper.load();
    }
}
