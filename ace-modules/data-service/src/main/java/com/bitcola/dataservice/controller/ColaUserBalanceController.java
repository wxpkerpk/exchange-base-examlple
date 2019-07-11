package com.bitcola.dataservice.controller;

import com.bitcola.caculate.entity.CaculateParams;
import com.bitcola.caculate.entity.ColaOrder;
import com.bitcola.caculate.entity.Vo.VoCaculateParams;
import com.bitcola.dataservice.biz.ColaOrderBiz;
import com.bitcola.dataservice.biz.ColaUserBalanceBiz;
import com.bitcola.dataservice.mapper.ColaUserBalanceMapper;
import com.bitcola.exchange.security.common.msg.ObjectRestResponse;
import com.bitcola.me.entity.ColaCoin;
import com.bitcola.me.entity.ColaMeBalance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * @author:wx
 * @description:controller
 * @create:2018-07-31  19:44
 */
@RestController
@Slf4j
@RequestMapping("balance")
public class ColaUserBalanceController {
    @Autowired
    ColaUserBalanceBiz colaUserBalanceBiz;
    @Autowired
    ColaOrderBiz colaOrderBiz;

    @Autowired
    ColaUserBalanceMapper colaUserBalanceMapper;
    @RequestMapping(value = "getUserBanlance",method = RequestMethod.GET)
    public ColaMeBalance getUserBanlance(String userId,String code){
        ColaCoin coin =  colaUserBalanceMapper.getCoin(code);
        ColaMeBalance colaMeBalance = colaUserBalanceMapper.selectBalance(userId, code);
        // 不能四舍五入,必须向下取整,不然用户输入最大金额进行交易时会出现余额不足
        colaMeBalance.setBalanceAvailable(colaMeBalance.getBalanceAvailable().setScale(coin.getPrec(), RoundingMode.DOWN));
        colaMeBalance.setBalanceFrozen(colaMeBalance.getBalanceFrozen().setScale(coin.getPrec(), RoundingMode.DOWN));
        return colaMeBalance;

    }



    @RequestMapping(value = "insert_order", method = RequestMethod.POST)
    public ObjectRestResponse insertOrder(ColaOrder colaOrder) {
        colaOrderBiz.insert(colaOrder);
        return new ObjectRestResponse<>().data(0);

    }

    @RequestMapping(value = "reduceOrder", method = RequestMethod.POST)
    public ObjectRestResponse reduceOrder(ArrayList<ColaOrder> colaOrders) {
        try {
            colaOrderBiz.reduceCount(colaOrders);
        } catch (Exception e) {
            log.error("用户余额不足或者 其他异常");
            return new ObjectRestResponse<>().data(-1);
        }
        return new ObjectRestResponse<>().data(0);

    }

    @RequestMapping(value = "makeOrder", method = RequestMethod.POST)
    public ObjectRestResponse makeOrder(String userId, BigDecimal price, String code, BigDecimal count, BigDecimal total, String type) {
        ColaOrder colaOrder=null;
        try {
             colaOrder= colaUserBalanceBiz.makeOrder(userId, code, price, count,total,type);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("用户余额不足或者 其他异常  :  "+e);
        }
        return new ObjectRestResponse<ColaOrder>().data(colaOrder);

    }



    @RequestMapping(value = "getUserMoneyPassword",method = RequestMethod.GET)
    public String getUserMoneyPassword(String id)
    {
        return colaUserBalanceBiz.getMoneyPassword(id);

    }

    @RequestMapping(value = "searchOrder",method = RequestMethod.GET)
    public List<ColaOrder>searchOrder(String userId,String code,String state,int start,int size ,String type,Long startTime,Long endTime,String pairL,String pairR){

        return colaOrderBiz.search(userId,code,state,start,size,type, startTime, endTime, pairL, pairR);

    }
    @RequestMapping(value = "countSelfOrders",method = RequestMethod.GET)
    public Long countSelfOrders(String userId,String code,String state,String type,Long startTime,Long endTime,String pairL,String pairR){
        return colaOrderBiz.countSelfOrders(userId,code,state,type, startTime, endTime, pairL, pairR);
    }


    //@RequestMapping(value = "getOrderById", method = RequestMethod.POST)
    //public ColaOrder getOrderById(String id)
    //{
    //    return colaOrderBiz.findById(id);
    //
    //}
    //@RequestMapping(value = "matchOrder", method = RequestMethod.POST)
    //public int matchOrder(@RequestBody VoCaculateParams caculateParams) {
    //    try {
    //        colaOrderBiz.matchOrder(caculateParams.getCompleted(),caculateParams.getTransForms(),caculateParams.getPaybacks(),caculateParams.getUnCompleted(),caculateParams.getExchangeLogs());
    //        return 0;
    //    } catch (Exception e) {
    //        e.printStackTrace();
    //    }
    //    return -1;
    //}


    @RequestMapping(value = "orderManagement", method = RequestMethod.GET)
    public List<Map<String, Object>> orderManagement(String userId,String code,String state,Integer page,
                                                     Integer size, String type,Long startTime,Long endTime,
                                                     String pairL,String pairR){
        return colaOrderBiz.orderManagement(userId,code,state,page,size,type,startTime,endTime,pairL,pairR);
    }
    @RequestMapping(value = "countOrderManagement", method = RequestMethod.GET)
    public Long countOrderManagement(String userId,String code,String state,
                                                          String type,Long startTime,Long endTime,
                                                     String pairL,String pairR){
        return colaOrderBiz.countOrderManagement(userId,code,state,type,startTime,endTime,pairL,pairR);
    }

    @RequestMapping(value = "orderHistory", method = RequestMethod.GET)
    public List<Map<String, Object>> orderHistory(String userId, Long timestamp, String code, String type,Integer size,Integer isPending){
        return colaOrderBiz.orderHistory(userId, timestamp, code, type,size,isPending);
    }


}
