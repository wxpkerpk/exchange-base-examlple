package com.bitcola.exchange.security.admin.rest;

import com.bitcola.exchange.security.admin.biz.ColaExchangeBiz;
import com.bitcola.exchange.security.admin.feign.IExchangeFeign;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.AdminQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 交易情况
 *
 * @author zkq
 * @create 2018-12-20 10:53
 **/
@RestController
@RequestMapping("cola/exchange")
public class ColaExchangeController {

    @Autowired
    ColaExchangeBiz biz;

    @Autowired
    IExchangeFeign exchangeFeign;

    @RequestMapping(value = "order",method = RequestMethod.GET)
    public TableResultResponse order(@RequestParam Map<String, Object> params){
        AdminQuery query = new AdminQuery(params);
        return biz.page(query);
    }

    @RequestMapping(value = "cancelOrder",method = RequestMethod.POST)
    public AppResponse cancelOrder(@RequestBody Map<String, String> params){
        String userId = params.get("userId");
        String orderId = params.get("orderId");
        // 调用交易模块取消订单
        exchangeFeign.cancelOrder(orderId,"feignKey");
        return AppResponse.ok();
    }




}
