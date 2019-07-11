package com.bitcola.chain.client;

import com.bitcola.chain.annotation.Params;
import com.bitcola.chain.annotation.RequestPath;
import com.bitcola.chain.feign.IDataServiceFeign;
import com.bitcola.chain.feign.IPushFeign;
import com.bitcola.exchange.security.common.msg.ColaChainDepositResponse;
import com.bitcola.exchange.security.common.msg.ColaChainOrder;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author zkq
 * @create 2019-01-19 16:28
 **/
@Log4j2
@RequestPath("chainClient")
public class ServerReceiveMessage {

    @Autowired
    IDataServiceFeign dataServiceFeign;

    @Autowired
    IPushFeign pushFeign;


    @RequestPath("deposit")
    public String deposit(@Params("deposit") ColaChainDepositResponse deposit){
        return dataServiceFeign.deposit(deposit);
    }

    @RequestPath("confirmNumber")
    public Integer confirmNumber(@Params("currentConfirmNumber") Integer currentConfirmNumber,@Params("orderId")String orderId){
        return dataServiceFeign.confirmNumber(currentConfirmNumber,orderId);
    }

    @RequestPath("completeDeposit")
    public boolean completeDeposit(@Params("orderId")String orderId){
        return dataServiceFeign.completeDeposit(orderId);
    }

    @RequestPath("getAddress")
    public List<String> getAddress(@Params("belong")String belong){
        return dataServiceFeign.getAddress(belong);
    }

    @RequestPath("getExportedOrder")
    public List<ColaChainOrder> getExportedOrder(@Params("belong")String belong){
        return dataServiceFeign.getExportedOrder(belong);
    }

    @RequestPath("dealWithdraw")
    public boolean dealWithdraw(@Params("response")ColaChainWithdrawResponse response){
        return dataServiceFeign.dealWithdraw(response);
    }

    @RequestPath("smsEarlyWarning")
    public boolean smsEarlyWarning(@Params("message")String message,@Params("telephone")String telephone){
        pushFeign.smsWarning(message,telephone);
        return true;
    }


}
