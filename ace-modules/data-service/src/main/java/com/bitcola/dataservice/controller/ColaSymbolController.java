package com.bitcola.dataservice.controller;

/*
 * @author:wx
 * @description:
 * @create:2018-08-28  20:43
 */

import com.bitcola.caculate.entity.DepthData;
import com.bitcola.dataservice.biz.ColaOrderBiz;
import com.bitcola.dataservice.biz.ColaSymbolBiz;
import com.bitcola.dataservice.exception.LACK_BALANCE_EXCEPTION;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.me.entity.ColaCoinSymbol;
import com.bitcola.me.entity.ColaCoinUserchoose;
import com.bitcola.me.entity.ColaUserChooseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("symbol")
public class ColaSymbolController {

    @Autowired
    ColaSymbolBiz colaSymbolBiz;
    @Autowired
    ColaOrderBiz colaOrderBiz;

    @RequestMapping("getSymbols")
    public List<String> getSymbols(){
        return colaSymbolBiz.getSymbols();
    }

    @RequestMapping("getCoinSymbolBySymbol")
    public AppResponse getCoinSymbolBySymbol(String symbol){
        List<ColaCoinSymbol> list = colaSymbolBiz.selectBySymbol(symbol);
        return AppResponse.ok().data(list);
    }
    @RequestMapping("getAllSymbol")
    public List<String> getAllSymbol()
    {
        return colaSymbolBiz.getAllSymbol();
    }
    @RequestMapping("getDepth")
    public DepthData getDepth(String code,int limit,double precision,double minCountPrecision,long time)
    {
        return colaOrderBiz.selectDepth(code,limit,precision,minCountPrecision, time);

    }
    @RequestMapping("cancelOrder")
    public String deleteOrder(String id) throws LACK_BALANCE_EXCEPTION {
        colaOrderBiz.cancelOrder(id);
        return id;
    }
    @RequestMapping("/getPair")
    ColaCoinSymbol getSymbol(@RequestParam(value = "pair")String pair){
        String codes[]=pair.split("_");
        return colaSymbolBiz.getPair(codes[1],codes[0]);
    }


    /**
     * 自选列表
     * @return
     */
    @RequestMapping(value = "list",method = RequestMethod.GET)
    public  List<ColaUserChooseVo> list(String userId){
        List<ColaUserChooseVo> list = colaSymbolBiz.list(userId);
        return list;
    }



}
