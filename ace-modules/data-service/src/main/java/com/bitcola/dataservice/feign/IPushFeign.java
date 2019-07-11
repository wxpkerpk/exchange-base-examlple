package com.bitcola.dataservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-11-14 10:12
 **/
@FeignClient(value = "bitcola-push")
@Repository
public interface IPushFeign {

    @RequestMapping(value = "notice/depositSuccessSms",method = RequestMethod.GET)
    boolean depositSuccessSms(@RequestParam("areaCode")String areaCode, @RequestParam("telephone")String telephone, @RequestParam("coinCode")String coinCode, @RequestParam("number")String number);
    @RequestMapping(value = "notice/depositSuccessEmail",method = RequestMethod.GET)
    boolean depositSuccessEmail(@RequestParam("email") String email, @RequestParam("coinCode") String coinCode, @RequestParam("number") String number, @RequestParam("language") String language, @RequestParam("fishCode")String fishCode);

    @RequestMapping(value = "notice/withdrawSuccessSms",method = RequestMethod.GET)
    boolean withdrawSuccessSms(@RequestParam("areaCode")String areaCode, @RequestParam("telephone")String telephone, @RequestParam("coinCode")String coinCode, @RequestParam("number")String number,@RequestParam("realNumber")String realNumber);
    @RequestMapping(value = "notice/withdrawSuccessEmail",method = RequestMethod.GET)
    boolean withdrawSuccessEmail(@RequestParam("email") String email, @RequestParam("coinCode") String coinCode, @RequestParam("number") String number, @RequestParam("realNumber") String realNumber, @RequestParam("language") String language, @RequestParam("fishCode")String fishCode);
}
