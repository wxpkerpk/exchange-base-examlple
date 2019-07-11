package com.bitcola.chain.controller;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.chain.annotation.Params;
import com.bitcola.chain.annotation.RequestPath;
import com.bitcola.exchange.security.common.msg.ColaChainDepositResponse;
import com.bitcola.exchange.security.common.msg.ColaChainOrder;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;

import java.util.List;


@RequestPath("chainClient")
public interface ChainSendMessage {

    @RequestPath("deposit")
    String deposit(@Params("deposit") ColaChainDepositResponse deposit);

    @RequestPath("confirmNumber")
    Integer confirmNumber(@Params("currentConfirmNumber") Integer currentConfirmNumber,@Params("orderId")String orderId);

    @RequestPath("completeDeposit")
    boolean completeDeposit(@Params("orderId")String orderId);

    @RequestPath("getAddress")
    List<String> getAddress(@Params("belong")String belong);

    @RequestPath("getExportedOrder")
    List<JSONObject> getExportedOrder(@Params("belong")String belong);

    @RequestPath("dealWithdraw")
    boolean dealWithdraw(@Params("response") ColaChainWithdrawResponse response);

    @RequestPath("smsEarlyWarning")
    boolean smsEarlyWarning(@Params("message")String message,@Params("telephone")String telephone);
}
