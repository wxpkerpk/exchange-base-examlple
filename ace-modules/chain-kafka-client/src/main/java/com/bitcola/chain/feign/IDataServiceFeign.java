package com.bitcola.chain.feign;


import com.bitcola.exchange.security.common.msg.ColaChainDepositResponse;
import com.bitcola.exchange.security.common.msg.ColaChainOrder;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author zkq
 * @create 2018-11-08 11:53
 **/
@FeignClient(value = "dataservice")
@Service
public interface IDataServiceFeign {

    @RequestMapping(value = "chain/deposit",method = RequestMethod.POST)
    String deposit(@RequestBody ColaChainDepositResponse deposit);

    @RequestMapping(value = "chain/confirmNumber",method = RequestMethod.GET)
    Integer confirmNumber(@RequestParam("currentConfirmNumber")Integer currentConfirmNumber, @RequestParam("orderId")String orderId);

    @RequestMapping(value = "chain/completeDeposit",method = RequestMethod.GET)
    boolean completeDeposit(@RequestParam("orderId")String orderId);

    @RequestMapping(value = "chain/getAddress",method = RequestMethod.GET)
    List<String> getAddress(@RequestParam("module")String module);

    @RequestMapping(value = "chain/getExportedOrder",method = RequestMethod.GET)
    List<ColaChainOrder> getExportedOrder(@RequestParam("belong")String belong);

    @RequestMapping(value = "chain/dealWithdraw",method = RequestMethod.POST)
    boolean dealWithdraw(@RequestBody ColaChainWithdrawResponse response);
}
