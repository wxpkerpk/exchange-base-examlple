package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.me.biz.ColaTokenDividendsBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * cola token 分红
 * @author zkq
 * @create 2018-10-26 15:05
 **/
@RestController
@RequestMapping("colaToken")
public class ColaTokenDividendsController {

    @Autowired
    ColaTokenDividendsBiz biz;

    /**
     * 返回 数量 , 预估值 USDT  , 分红总量(预估 USDT), 每1000 COLA 分红数量(预估 USDT)
     * @return
     */
    @RequestMapping("info")
    public AppResponse info(){
        Map<String,Object> result = biz.info();
        return AppResponse.ok().data(result);
    }

    @RequestMapping("list")
    public TableResultResponse list(String keyWord,int page,int limit){
        TableResultResponse result = biz.list(keyWord,page,limit);
        return result;
    }



}


