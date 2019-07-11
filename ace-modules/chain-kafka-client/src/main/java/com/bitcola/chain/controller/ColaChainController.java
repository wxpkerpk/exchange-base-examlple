package com.bitcola.chain.controller;

import com.bitcola.chain.client.ServerSendMessage;
import com.bitcola.exchange.security.common.msg.ColaChainBalance;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2019-01-21 17:22
 **/
@RestController
@RequestMapping("chain")
public class ColaChainController {

    @Autowired
    ServerSendMessage serverSendMessage;

    @RequestMapping("newAccount")
    public String newAccount(String module,String coinCode){
        return serverSendMessage.newAccount(module,coinCode);
    }

    @RequestMapping(value = "getChainBalance",method = RequestMethod.GET)
    public ColaChainBalance getChainBalance(String module, String coinCode, String feeCoinCode){
        return serverSendMessage.getChainBalance(module,coinCode,feeCoinCode);
    }

    @RequestMapping(value = "checkAddress",method = RequestMethod.GET)
    public boolean checkAddress(String module, String coinCode, String address){
        return serverSendMessage.checkAddress(module,coinCode,address);
    }



}
