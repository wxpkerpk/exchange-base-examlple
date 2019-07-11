package com.bitcola.exchange.security.admin.biz;

import com.bitcola.exchange.security.admin.mapper.ColaExchangeMapper;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.AdminQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-12-20 10:54
 **/
@Service
public class ColaExchangeBiz {
    @Autowired
    ColaExchangeMapper mapper;
    public TableResultResponse page(AdminQuery query) {
        Long total = mapper.total(query);
        List<Map<String,Object>> list = mapper.page(query);
        return new TableResultResponse(total,list);
    }
}
