package com.bitcola.exchange.security.admin.biz;

import com.bitcola.exchange.security.admin.mapper.ColaUserLimitMapper;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import com.bitcola.me.entity.ColaUserLimit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zkq
 * @create 2018-12-24 20:47
 **/
@Service
public class ColaUserLimitBiz extends BaseBiz<ColaUserLimitMapper,ColaUserLimit> {

    @Autowired
    ColaUserLimitMapper userLimitMapper;


    public ColaUserLimit getUserLimit(String userId, String module) {
        return userLimitMapper.getUserLimit(userId,module);
    }
}
