package com.bitcola.chain.client;

import com.bitcola.chain.annotation.Params;
import com.bitcola.chain.annotation.RequestPath;
import com.bitcola.exchange.security.common.msg.ColaChainBalance;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;

import java.math.BigDecimal;

@RequestPath("chainServer")
public interface ServerSendMessage {

    @RequestPath("newAccount")
    public String newAccount(@Params("module")String module,@Params("coinCode")String coinCode);

    @RequestPath("getChainBalance")
    ColaChainBalance getChainBalance(@Params("module")String module, @Params("coinCode")String coinCode,@Params("feeCoinCode")String feeCoinCode);

    @RequestPath("checkAddress")
    boolean checkAddress(@Params("module")String module, @Params("coinCode")String coinCode, @Params("address")String address);
}
