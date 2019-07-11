package com.bitcola.exchange.security.admin.biz;

import com.bitcola.exchange.security.admin.entity.GateLog;
import com.bitcola.exchange.security.admin.mapper.GateLogMapper;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * ${DESCRIPTION}
 *
 * @author wx
 * @create 2017-07-01 14:36
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class GateLogBiz extends BaseBiz<GateLogMapper,GateLog> {

    @Override
    public void insert(GateLog entity) {
        mapper.insert(entity);
    }

    @Override
    public void insertSelective(GateLog entity) {
        entity.setId(UUID.randomUUID().toString());
        mapper.insertSelective(entity);
    }
}
