package com.bitcola.chain.server.xlm;

import com.bitcola.chain.chain.eos.EosCore;
import com.bitcola.chain.chain.eos.entity.EosTransaction;
import com.bitcola.chain.chain.stellar.StellarCore;
import com.bitcola.chain.chain.stellar.XlmTransaction;
import com.bitcola.chain.chain.xem.XemCore;
import com.bitcola.chain.constant.DepositStatusConstant;
import com.bitcola.chain.entity.ColaChainKey;
import com.bitcola.chain.entity.ColaChainXemToken;
import com.bitcola.chain.entity.ColaChainXlmToken;
import com.bitcola.chain.mapper.ColaChainDepositMapper;
import com.bitcola.chain.mapper.ColaChainKeyMapper;
import com.bitcola.chain.mapper.ColaChainXlmTokenMapper;
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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zkq
 * @create 2019-01-25 12:11
 **/
@Log4j2
@Component
public class XlmChainServer extends BaseChainServer {

    @Autowired
    ColaChainKeyMapper keyMapper;

    @Autowired
    ColaChainXlmTokenMapper xlmTokenMapper;

    @Autowired
    ColaChainDepositMapper depositMapper;

    @Value("${bitcola.chain.dev}")
    Boolean dev;

    @Value("${bitcola.chain.password}")
    String encoderKey;

    @Override
    protected void run() throws Throwable {
        List<ColaChainDepositResponse> unConfirmDeposit = depositMapper.unConfirm(getModuleName());
        for (ColaChainDepositResponse depositResponse : unConfirmDeposit) {
            unConfirm.put(depositResponse.getHash(),0);
        }
        scheduler.scheduleWithFixedDelay(()->{
            try {
                List<XlmTransaction> transactions = StellarCore.getTransactions();
                for (XlmTransaction transaction : transactions) {
                    if ((!dev && MemoUtil.isProdMemo(transaction.getMemo())) ||
                            (dev&&MemoUtil.isDevMemo(transaction.getMemo()))){
                        String hash = transaction.getHash();
                        boolean exists = depositMapper.existsWithPrimaryKey(hash);
                        if (!exists){
                            // 根据 tokenName 获取 coinCode
                            ColaChainDepositResponse deposit = new ColaChainDepositResponse();
                            Boolean isToken = transaction.getIsToken();
                            if (!isToken){
                                deposit.setCoinCode(getModuleName());
                            } else {
                                String coinCode = xlmTokenMapper.getCoinCodeByTokenName(transaction.getTokenCode(), transaction.getTokenIssuer());
                                deposit.setCoinCode(coinCode);
                            }
                            BigDecimal amount = transaction.getAmount();
                            if (StringUtils.isNotBlank(deposit.getCoinCode()) && amount.compareTo(getDepositMin(deposit.getCoinCode())) >= 0){
                                log.info("扫描到 XLM 交易: "+hash);
                                deposit.setHash(hash);
                                deposit.setAmount(transaction.getAmount());
                                deposit.setTimestamp(System.currentTimeMillis());
                                deposit.setModule(getModuleName());
                                deposit.setToAddress(transaction.getTo());
                                deposit.setStatus(DepositStatusConstant.NOT_RECORD);
                                deposit.setMemo(transaction.getMemo());
                                String orderId = deposit(deposit);
                                if (StringUtils.isNotBlank(orderId)){
                                    unConfirm.put(hash,0);
                                }
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
        return "XLM";
    }

    @Override
    public String newAccount(String coinCode) throws Throwable {
        String account = StellarCore.newAccount();
        address.add(account);
        return account;
    }

    @Override
    public boolean checkAddress(String address) {
        return StellarCore.checkAddress(address);
    }

    @Override
    public ColaChainWithdrawResponse withdraw(String coinCode, String address, BigDecimal number, String memo) throws Throwable {
        ColaChainWithdrawResponse response;
        ColaChainKey colaChainKey = keyMapper.selectByPrimaryKey(getModuleName());
        String key = colaChainKey.getKey();
        String password = AESUtil.decrypt(key,encoderKey);
        // 判断是否为代币
        if (getModuleName().equalsIgnoreCase(coinCode)){
            response = StellarCore.withdraw(address,number,memo,null,null,password);
        } else {
            ColaChainXlmToken token = xlmTokenMapper.selectByPrimaryKey(coinCode);
            response = StellarCore.withdraw(address,number,memo,token.getTokenCode(),token.getTokenIssuer(),password);
        }
        response.setFeeCoinCode(getModuleName());
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
                boolean hs = StellarCore.hashSuccess(hash);
                if (hs){
                    iterator.remove();
                    deposit.setStatus(DepositStatusConstant.CONFIRM);
                    depositMapper.updateByPrimaryKeySelective(deposit);
                    boolean success = completeDeposit(deposit.getOrderId());
                }
            }
        }
    }

    @Override
    public ColaChainBalance getChainBalance(String coinCode, String feeCoinCode)throws Exception{
        ColaChainBalance chainBalance = new ColaChainBalance();
        chainBalance.setCoinCode(coinCode);
        chainBalance.setFeeCoinCode(feeCoinCode);
        BigDecimal xlmBalance = StellarCore.getXLMBalance();
        chainBalance.setFeeBalance(xlmBalance);
        if (coinCode.equals(feeCoinCode)){
            chainBalance.setBalance(xlmBalance);
        } else {
            ColaChainXlmToken token = xlmTokenMapper.selectByPrimaryKey(coinCode);
            BigDecimal mosaicBalance = StellarCore.getTokenBalance(token.getTokenCode(),token.getTokenIssuer());
            chainBalance.setBalance(mosaicBalance);
        }
        return chainBalance;
    }
}
