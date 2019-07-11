package com.bitcola.chain.server.newton;

import com.bitcola.chain.chain.newton.NewTonCore;
import com.bitcola.chain.chain.newton.entity.NewTonTransaction;
import com.bitcola.chain.constant.DepositStatusConstant;
import com.bitcola.chain.mapper.ColaChainDepositMapper;
import com.bitcola.chain.mapper.ColaChainKeyMapper;
import com.bitcola.chain.server.BaseChainServer;
import com.bitcola.chain.util.MemoUtil;
import com.bitcola.exchange.security.common.msg.ColaChainBalance;
import com.bitcola.exchange.security.common.msg.ColaChainDepositResponse;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zkq
 * @create 2019-01-24 18:15
 **/
@Component
@Log4j2
public class NewTonChainServer extends BaseChainServer {

    @Autowired
    ColaChainDepositMapper depositMapper;

    @Autowired
    ColaChainKeyMapper keyMapper;

    @Value("${bitcola.chain.password}")
    String encoderKey;
    @Value("${bitcola.chain.dev}")
    Boolean dev;

    @Override
    protected void run() throws Throwable {
        List<ColaChainDepositResponse> unConfirmDeposit = depositMapper.unConfirm(getModuleName());
        for (ColaChainDepositResponse depositResponse : unConfirmDeposit) {
            unConfirm.put(depositResponse.getHash(),0);
        }
        new Thread(()->{
            while(true){
                try {
                    List<NewTonTransaction> list = NewTonCore.getIncomeTransaction();
                    for (NewTonTransaction transaction : list) {
                        String memo = NewTonCore.getMemo(transaction.getData());
                        if ((!dev && MemoUtil.isProdMemo(memo)) || (dev && MemoUtil.isDevMemo(memo))) {
                            String hash = transaction.getTxid();
                            BigDecimal number = transaction.getValue();
                            boolean exists = depositMapper.existsWithPrimaryKey(hash);
                            if (!exists && number.compareTo(getDepositMin(getModuleName()))>=0){
                                ColaChainDepositResponse deposit = new ColaChainDepositResponse();
                                deposit.setHash(hash);
                                deposit.setAmount(number);
                                deposit.setTimestamp(System.currentTimeMillis());
                                deposit.setModule(getModuleName());
                                deposit.setCoinCode(getModuleName());
                                deposit.setToAddress(transaction.getTo_addr());
                                deposit.setStatus(DepositStatusConstant.NOT_RECORD);
                                deposit.setMemo(memo);
                                String orderId = deposit(deposit);
                                if (StringUtils.isNotBlank(orderId)){
                                    unConfirm.put(hash,0);
                                }
                            }
                        }
                    }
                    Thread.sleep(30 * 1000);
                } catch (Throwable throwable) {
                    log.error(throwable.getMessage(),throwable);
                }
            }
        }).start();
    }

    @Override
    public String getModuleName() {
        return "NEW";
    }

    @Override
    public String newAccount(String coinCode) throws Throwable {
        address.add(NewTonCore.ADDRESS);
        return NewTonCore.ADDRESS;
    }

    @Override
    public boolean checkAddress(String address) {
        return NewTonCore.checkAddress(address);
    }

    @Override
    public ColaChainWithdrawResponse withdraw(String coinCode, String address, BigDecimal number, String memo) throws Throwable {
        ColaChainWithdrawResponse response = new ColaChainWithdrawResponse();
        response.setSuccess(false);
        return response;
    }


    @Override
    @Scheduled(cron = "0/10 * * * * ?")
    public void confirm() throws Throwable {
        Iterator<String> iterator = unConfirm.keySet().iterator();
        while (iterator.hasNext()){
            String hash = iterator.next();
            ColaChainDepositResponse deposit = depositMapper.selectByPrimaryKey(hash);
            if (System.currentTimeMillis()-deposit.getTimestamp()>60*1000){
                boolean hs = NewTonCore.hashSuccess(hash);
                if (hs){
                    deposit.setStatus(DepositStatusConstant.CONFIRM);
                    depositMapper.updateByPrimaryKeySelective(deposit);
                    iterator.remove();
                    completeDeposit(deposit.getOrderId());
                }
            }
        }
    }

    @Override
    public ColaChainBalance getChainBalance(String coinCode, String feeCoinCode) throws Exception{
        ColaChainBalance chainBalance = new ColaChainBalance();
        chainBalance.setCoinCode(coinCode);
        chainBalance.setFeeCoinCode(feeCoinCode);
        BigDecimal balance = NewTonCore.getBalance();
        chainBalance.setFeeBalance(balance);
        chainBalance.setBalance(balance);
        chainBalance.setModule(getModuleName());
        return chainBalance;
    }

}
