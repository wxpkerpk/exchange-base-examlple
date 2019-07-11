package com.bitcola.exchange.security.me.biz;

import com.bitcola.exchange.security.common.biz.BaseBiz;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.me.mapper.ColaMeWorkOrderMapper;
import com.bitcola.me.entity.ColaMeWorkOrder;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 
 *
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-09-12 19:12:17
 */
@Service
public class ColaMeWorkOrderBiz extends BaseBiz<ColaMeWorkOrderMapper, ColaMeWorkOrder> {
    public List<ColaMeWorkOrder> list(Long timestamp, Integer size) {
        return mapper.list(timestamp, BaseContextHandler.getUserID(),size);
    }
}