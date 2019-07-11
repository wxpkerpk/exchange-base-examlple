package com.bitcola.chain.server.nxt;

import com.bitcola.chain.chain.nxt.NxtCore;
import com.bitcola.chain.chain.nxt.entity.Transaction;
import com.bitcola.chain.chain.tera.TeraCore;
import com.bitcola.chain.chain.tera.entity.Account;
import com.bitcola.chain.chain.tera.entity.History;
import com.bitcola.chain.chain.tera.entity.SendResult;
import com.bitcola.chain.constant.DepositStatusConstant;
import com.bitcola.chain.entity.ColaChainKey;
import com.bitcola.chain.entity.ColaChainTeraKey;
import com.bitcola.chain.mapper.ColaChainDepositMapper;
import com.bitcola.chain.mapper.ColaChainKeyMapper;
import com.bitcola.chain.mapper.ColaChainTeraKeyMapper;
import com.bitcola.chain.server.BaseChainServer;
import com.bitcola.chain.util.AESUtil;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author zkq
 * @create 2019-01-24 18:15
 **/
@Component
@Log4j2
public class NxtChainServer extends BaseChainServer {

    @Autowired
    ColaChainDepositMapper depositMapper;

    @Autowired
    ColaChainKeyMapper keyMapper;

    @Value("${bitcola.chain.password}")
    String encoderKey;
    @Value("${bitcola.chain.dev}")
    Boolean dev;

    private Set<String> hashSet = new HashSet<>();

    @Override
    protected void run() throws Throwable {
        List<ColaChainDepositResponse> unConfirmDeposit = depositMapper.unConfirm(getModuleName());
        for (ColaChainDepositResponse depositResponse : unConfirmDeposit) {
            unConfirm.put(depositResponse.getHash(),0);
        }
        ColaChainKey colaChainKey = keyMapper.selectByPrimaryKey(getModuleName());
        String key = colaChainKey.getKey();
        String password = AESUtil.decrypt(key,encoderKey);
        scheduler.scheduleWithFixedDelay(()->{
            try {
                List<Transaction> transactions = NxtCore.getTransactions(20,password);
                for (Transaction transaction : transactions) {
                    if ((!dev && MemoUtil.isProdMemo(transaction.getAttachment().getMessage())) ||
                            (dev&&MemoUtil.isDevMemo(transaction.getAttachment().getMessage()))){
                        String hash = transaction.getTransaction();
                        BigDecimal number = NxtCore.getNxtNumber(transaction.getAmountNQT());
                        if (!hashExist(hash) && number.compareTo(getDepositMin(getModuleName()))>=0){
                            ColaChainDepositResponse deposit = new ColaChainDepositResponse();
                            deposit.setHash(hash);
                            deposit.setAmount(number);
                            deposit.setTimestamp(System.currentTimeMillis());
                            deposit.setModule(getModuleName());
                            deposit.setCoinCode(getModuleName());
                            deposit.setToAddress(transaction.getRecipientRS());
                            deposit.setStatus(DepositStatusConstant.NOT_RECORD);
                            deposit.setMemo(transaction.getAttachment().getMessage());
                            String orderId = deposit(deposit);
                            if (StringUtils.isNotBlank(orderId)){
                                unConfirm.put(hash,0);
                            }
                        }
                    }
                }
            } catch (Throwable throwable) {
                log.error(throwable.getMessage(),throwable);
            }
        },0,60, TimeUnit.SECONDS);
    }

    @Override
    public String getModuleName() {
        return "NXT";
    }

    @Override
    public String newAccount(String coinCode) {
        return NxtCore.newAccount();
    }

    @Override
    public boolean checkAddress(String address) {
        return NxtCore.checkAddress(address);
    }


    @Override
    public ColaChainWithdrawResponse withdraw(String coinCode, String address, BigDecimal number, String memo) {
        ColaChainKey colaChainKey = keyMapper.selectByPrimaryKey(getModuleName());
        String key = colaChainKey.getKey();
        String password = AESUtil.decrypt(key,encoderKey);
        return NxtCore.send(address,number,memo,password);
    }


    @Override
    @Scheduled(cron = "0/30 * * * * ?")
    public void confirm() throws Throwable {
        Iterator<String> iterator = unConfirm.keySet().iterator();
        while (iterator.hasNext()){
            String hash = iterator.next();
            ColaChainDepositResponse deposit = depositMapper.selectByPrimaryKey(hash);
            if (System.currentTimeMillis()-deposit.getTimestamp()>60*1000){
                Transaction transaction = NxtCore.getTransaction(hash);
                if (transaction.getConfirmations() >= getConfirmNumber(getModuleName())){
                    deposit.setStatus(DepositStatusConstant.CONFIRM);
                    depositMapper.updateByPrimaryKeySelective(deposit);
                    iterator.remove();
                    completeDeposit(deposit.getOrderId());
                } else {
                    confirmNumber(transaction.getConfirmations(),deposit.getOrderId());
                    unConfirm.put(hash,transaction.getConfirmations());
                }
            }
        }
    }

    @Override
    public ColaChainBalance getChainBalance(String coinCode, String feeCoinCode) {
        ColaChainBalance chainBalance = new ColaChainBalance();
        chainBalance.setCoinCode(coinCode);
        chainBalance.setFeeCoinCode(feeCoinCode);
        BigDecimal balance = NxtCore.getBalance();
        if (coinCode.equals(feeCoinCode)){
            chainBalance.setBalance(balance);
            chainBalance.setFeeBalance(balance);
        }
        return chainBalance;
    }

    /**
     * 判断 hash 是否已经被处理过
     * @param hash
     * @return
     */
    private boolean hashExist(String hash){
        boolean contains = hashSet.contains(hash);
        if (!contains){
            contains = depositMapper.existsWithPrimaryKey(hash);
        }
        if (!contains){
            hashSet.add(hash);
        }
        return contains;
    }



}
