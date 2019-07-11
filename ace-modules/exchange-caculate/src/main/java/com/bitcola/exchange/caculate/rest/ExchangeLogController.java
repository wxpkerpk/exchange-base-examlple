package com.bitcola.exchange.caculate.rest;

import com.bitcola.caculate.entity.ExchangeLog;
import com.bitcola.exchange.caculate.dataservice.ExchangeLogService;
import com.bitcola.exchange.security.auth.client.annotation.IgnoreClientToken;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.List;

/*
 * @author:wx
 * @description:
 * @create:2018-09-04  00:26
 */
@RequestMapping(value = "exchangelog")
@IgnoreClientToken
@RestController

public class ExchangeLogController {
    @Autowired
    @Lazy
    private ExchangeLogService exchangeLogService;


    @RequestMapping("selectById")
    public AppResponse selectById(String orderId){
        return AppResponse.ok().data( exchangeLogService.selectById(orderId));
    }



    @RequestMapping("selectByUser")
    //根据用户获取交易记录
    public AppResponse selectByUser(String code, int page, int size)
    {

        int start=(page-1)*size;
        String userId=BaseContextHandler.getUserID();


       return AppResponse.ok().data( exchangeLogService.selectByUser(code,userId,start,size));

    }


    @RequestMapping("selectByCode")
    //根据交易对获取交易记录

     public AppResponse selectByCode(String code,int page,int size)
    {
        int start=(page-1)*size;

        return  AppResponse.ok().data(exchangeLogService.selectByCode(code,start,size));
    }
}
