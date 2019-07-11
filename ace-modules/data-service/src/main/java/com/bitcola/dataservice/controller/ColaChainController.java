package com.bitcola.dataservice.controller;

import com.bitcola.dataservice.biz.ColaChainBiz;
import com.bitcola.dataservice.dto.WithdrawDto;
import com.bitcola.exchange.security.common.msg.ColaChainDepositResponse;
import com.bitcola.exchange.security.common.msg.ColaChainOrder;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 链
 *
 * @author zkq
 * @create 2018-11-12 15:22
 **/
@RequestMapping("chain")
@RestController
public class ColaChainController {

    @Autowired
    ColaChainBiz biz;

    /**
     * 新版
     * @param deposit
     * @return
     */
    @RequestMapping(value = "deposit",method = RequestMethod.POST)
    public String deposit(@RequestBody ColaChainDepositResponse deposit){
        return biz.deposit(deposit);
    }

    @RequestMapping(value = "confirmNumber",method = RequestMethod.GET)
    public Integer confirmNumber(@RequestParam("currentConfirmNumber")Integer currentConfirmNumber, @RequestParam("orderId")String orderId){
        return biz.confirmNumber(currentConfirmNumber,orderId);
    }

    @RequestMapping(value = "completeDeposit",method = RequestMethod.GET)
    public boolean completeDeposit(@RequestParam("orderId")String orderId){
        return biz.completeDepositV2(orderId);
    }

    @RequestMapping(value = "getAddress",method = RequestMethod.GET)
    public List<String> getAddress(@RequestParam("module")String module){
        return biz.getScanAddress(module);
    }

    @RequestMapping(value = "getExportedOrder",method = RequestMethod.GET)
    public List<ColaChainOrder> getExportedOrder(@RequestParam("belong")String belong){
        return biz.getExportedOrder(belong);
    }

    @RequestMapping(value = "dealWithdraw",method = RequestMethod.POST)
    public boolean dealWithdraw(@RequestBody ColaChainWithdrawResponse response){
        WithdrawDto order = biz.getWithdrawOrder(response.getOrderId());
        if (response.isSuccess()){
            biz.withdrawSuccess(response,order);
        } else {
            biz.withdrawFailed(response,order);
        }
        return true;
    }

}
