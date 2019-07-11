package com.bitcola.dataservice.controller;

import com.bitcola.caculate.entity.ExchangeLog;
import com.bitcola.dataservice.mapper.ColaCaculateExchangeLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * @author:wx
 * @description:
 * @create:2018-09-04  00:08
 */
@RestController
@Slf4j
@RequestMapping("exchangelog")
public class ColaExchangeLogController {
    @Autowired
    ColaCaculateExchangeLogMapper colaCaculateExchangeLogMapper;

    @RequestMapping("selectByUser")

    List<Map> selectByUser(String code,String userId,int start,int size)
    {
        return colaCaculateExchangeLogMapper.selectByUserId(code,userId,start,size).stream().map(ExchangeLog::toMap).collect(Collectors.toList());

    }
    @RequestMapping("selectById")
    List<Map> selectById(@RequestParam("orderId")String orderId){
        return colaCaculateExchangeLogMapper.selectById(orderId).stream().map(ExchangeLog::toMap).collect(Collectors.toList());
    }

    @RequestMapping("selectByCode")

    List<Map> selectByCode(String code,int start,int size)
    {
        return colaCaculateExchangeLogMapper.selectByCode(code,start,size).stream().map(ExchangeLog::toMap).collect(Collectors.toList());
    }



}
