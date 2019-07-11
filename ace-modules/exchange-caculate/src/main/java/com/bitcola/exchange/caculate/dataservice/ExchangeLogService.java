package com.bitcola.exchange.caculate.dataservice;

import com.bitcola.caculate.entity.ExchangeLog;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient("dataservice")
public interface ExchangeLogService {

    @RequestMapping("exchangelog/selectByUser")
    List<Map> selectByUser(@RequestParam(value = "code")String code,@RequestParam(value = "userId")String userId,@RequestParam(value = "start") int start,@RequestParam(value = "size") int size);



    @RequestMapping("exchangelog/selectByCode")
    List<Map> selectByCode(@RequestParam(value = "code")String code, @RequestParam(value = "start")int start, @RequestParam(value = "size")int size);

    @RequestMapping("exchangelog/selectById")
    List<Map> selectById(@RequestParam("orderId")String orderId);
}
