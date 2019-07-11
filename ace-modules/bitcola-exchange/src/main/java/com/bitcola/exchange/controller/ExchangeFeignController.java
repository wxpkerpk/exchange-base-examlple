package com.bitcola.exchange.controller;

import com.bitcola.exchange.biz.ColaExchangeBiz;
import com.bitcola.exchange.constant.OrderStatus;
import com.bitcola.exchange.mapper.OrderMapper;
import com.bitcola.exchange.message.OrderMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zkq
 * @create 2019-04-03 11:12
 **/
@RestController
@RequestMapping("feign")
public class ExchangeFeignController {

    @Autowired
    OrderMapper orderMapper;


    @Autowired
    ColaExchangeBiz biz;


    @RequestMapping("cancelOrder")
    public boolean cancelOrder(String orderId,String key){
        if ("feignKey".equals(key)){
            OrderMessage entity = orderMapper.selectByPrimaryKey(orderId);
            if (entity.getStatus().equals(OrderStatus.FULL_COMPLETED) || entity.getStatus().equals(OrderStatus.FULL_CANCELLED) || entity.getStatus().equals(OrderStatus.PARTIAL_CANCELLED)){
                return true;
            }
            biz.cancelOrder(entity);
            return true;
        } else {
            return false;
        }
    }
}
