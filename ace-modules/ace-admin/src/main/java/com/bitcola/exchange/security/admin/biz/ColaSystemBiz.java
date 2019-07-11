package com.bitcola.exchange.security.admin.biz;

import com.bitcola.exchange.security.admin.entity.SystemMaintain;
import com.bitcola.exchange.security.admin.mapper.ColaSystemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-11-29 10:55
 **/
@Service
public class ColaSystemBiz {
    @Autowired
    ColaSystemMapper mapper;
    public List<Map<String, Object>> status() {
        return mapper.status();
    }

    public void maintain(String module, String timestamp) {
        Long time = Long.valueOf(timestamp);
        SystemMaintain entity = new SystemMaintain();
        entity.setModule(module);
        entity.setTimestamp(time);
        entity.setStatus("0");
        mapper.updateByPrimaryKey(entity);
    }

    public void statusSuccess(String module) {
        SystemMaintain entity = new SystemMaintain();
        entity.setModule(module);
        entity.setStatus("1");
        mapper.updateByPrimaryKeySelective(entity);
    }
}
