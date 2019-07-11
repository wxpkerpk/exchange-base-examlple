package com.bitcola.chain.controller;

import com.bitcola.chain.annotation.Params;
import com.bitcola.chain.annotation.RequestPath;
import com.bitcola.chain.constant.DepositStatusConstant;
import com.bitcola.chain.entity.ColaChainWithdraw;
import com.bitcola.chain.init.GlobalChain;
import com.bitcola.chain.mapper.ColaChainWithdrawMapper;
import com.bitcola.chain.server.BaseChainServer;
import com.bitcola.exchange.security.common.msg.ColaChainBalance;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

/**
 *
 * @author zkq
 * @create 2019-01-19 16:28
 **/
@Log4j2
@RequestPath("chainServer")
public class ChainReceiveMessage {

    @Autowired
    ColaChainWithdrawMapper withdrawMapper;


    @RequestPath("newAccount")
    public  synchronized String newAccount(@Params("module") String module,@Params("coinCode")String coinCode) throws Throwable {
        BaseChainServer chainServer = GlobalChain.serverMap.get(module.toUpperCase());
        return chainServer.newAccount(coinCode);
    }

    @RequestPath("getChainBalance")
    public ColaChainBalance getChainBalance(@Params("module")String module, @Params("coinCode")String coinCode, @Params("feeCoinCode")String feeCoinCode) throws Throwable {
        BaseChainServer chainServer = GlobalChain.serverMap.get(module.toUpperCase());
        return chainServer.getChainBalance(coinCode,feeCoinCode);
    }


    @RequestPath("checkAddress")
    public boolean checkAddress(@Params("module")String module, @Params("coinCode")String coinCode, @Params("address")String address){
        BaseChainServer chainServer = GlobalChain.serverMap.get(module.toUpperCase());


        return chainServer.checkAddress(address);
    }

}
