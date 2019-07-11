package com.bitcola.exchange.security.me.feign;

import com.bitcola.chaindata.entity.WithdrawResponse;
import com.bitcola.exchange.security.common.msg.ColaChainBalance;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;
import org.apache.ibatis.annotations.Param;
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
@FeignClient(value = "chain-kafka-client")
@Repository
public interface IChainServiceFeign {

    @RequestMapping(value = "chain/newAccount",method = RequestMethod.GET)
    public String newAccount(@RequestParam("coinCode") String coinCode,@RequestParam("module")String module);

    @RequestMapping(value = "chain/withdraw",method = RequestMethod.GET)
    public ColaChainWithdrawResponse withdraw(@RequestParam("coinCode")String coinCode, @RequestParam("address")String address, @RequestParam("memo")String memo, @RequestParam("number")BigDecimal number, @RequestParam("module")String module,@RequestParam("orderId")String orderId);

    @RequestMapping(value = "chain/getChainBalance",method = RequestMethod.GET)
    ColaChainBalance getChainBalance(@RequestParam("module")String module, @RequestParam("coinCode")String coinCode, @RequestParam("feeCoinCode")String feeCoinCode);

    @RequestMapping(value = "chain/checkAddress",method = RequestMethod.GET)
    boolean checkAddress(@RequestParam("module")String module, @RequestParam("coinCode")String coinCode, @RequestParam("address")String address);



}
