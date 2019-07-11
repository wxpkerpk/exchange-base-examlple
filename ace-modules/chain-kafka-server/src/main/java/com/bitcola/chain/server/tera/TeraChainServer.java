package com.bitcola.chain.server.tera;

import com.bitcola.chain.chain.eos.EosCore;
import com.bitcola.chain.chain.eos.entity.EosTransaction;
import com.bitcola.chain.chain.eth.EthCore;
import com.bitcola.chain.chain.nxt.NxtCore;
import com.bitcola.chain.chain.tera.TeraCore;
import com.bitcola.chain.chain.tera.entity.Account;
import com.bitcola.chain.chain.tera.entity.History;
import com.bitcola.chain.chain.tera.entity.SendResult;
import com.bitcola.chain.constant.DepositStatusConstant;
import com.bitcola.chain.entity.ColaChainEosToken;
import com.bitcola.chain.entity.ColaChainEthKey;
import com.bitcola.chain.entity.ColaChainKey;
import com.bitcola.chain.entity.ColaChainTeraKey;
import com.bitcola.chain.mapper.ColaChainDepositMapper;
import com.bitcola.chain.mapper.ColaChainEosTokenMapper;
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
public class TeraChainServer extends BaseChainServer {

    @Autowired
    ColaChainDepositMapper depositMapper;

    @Autowired
    ColaChainKeyMapper keyMapper;

    @Autowired
    ColaChainTeraKeyMapper teraKeyMapper;

    @Value("${bitcola.chain.password}")
    String encoderKey;
    @Value("${bitcola.chain.dev}")
    Boolean dev;

    private Set<String> hashSet = new HashSet<>();
    private List<Account> accounts = new ArrayList<>();

    @Override
    protected void run() throws Throwable {
        address.addAll(getAddress());
        generateAccount();
        List<ColaChainDepositResponse> unConfirmDeposit = depositMapper.unConfirm(getModuleName());
        for (ColaChainDepositResponse depositResponse : unConfirmDeposit) {
            unConfirm.put(depositResponse.getHash(),0);
        }
        scheduler.scheduleWithFixedDelay(()->{
            try {
                for (String account : address) {
                    List<History> transactions = TeraCore.getTransactions(Long.valueOf(account));
                    for (History transaction : transactions) {
                        if (transaction.getDirect().equals(TeraCore.deposit)){ // 充值
                            BigDecimal number = transaction.getSumCOIN().add(TeraCore.getSumCENT(transaction.getSumCENT()));
                            if (!hashExist(transaction.getTxID()) && number.compareTo(getDepositMin(getModuleName()))>=0){
                                ColaChainDepositResponse deposit = new ColaChainDepositResponse();
                                deposit.setHash(transaction.getTxID());
                                deposit.setAmount(number);
                                deposit.setTimestamp(System.currentTimeMillis());
                                deposit.setModule(getModuleName());
                                deposit.setCoinCode("TERA");
                                deposit.setToAddress(account);
                                deposit.setStatus(DepositStatusConstant.NOT_RECORD);
                                String orderId = deposit(deposit); // 充值
                                if (StringUtils.isNotBlank(orderId)){
                                    unConfirm.put(transaction.getTxID(),0);
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
        return "TERA";
    }

    @Override
    public String newAccount(String coinCode) {
        if (accounts.size()==0){
            Account account = TeraCore.newAccount();
            accounts.add(account);
        }
        if (accounts.size() < 3){
            generateAccount();
        }
        Account account = accounts.remove(0);
        if (account.getResult() != 0){
            ColaChainTeraKey key = new ColaChainTeraKey();
            key.setAccountId(String.valueOf(account.getAccountID()));
            key.setPrivateKey(AESUtil.encrypt(account.getPrivateKey(),encoderKey));
            key.setPublicKey(account.getPublicKey());
            teraKeyMapper.insertSelective(key);
            address.add(key.getAccountId());
            return key.getAccountId();
        }
        throw new RuntimeException("The account creation transaction was already in this block: 请稍后再尝试");
    }

    @Override
    public boolean checkAddress(String address) {
        return TeraCore.checkAddress(address);
    }

    private void generateAccount() {
        new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                Account account = TeraCore.newAccount();
                if (account.getResult() != 0){
                    accounts.add(account);
                }
                try {
                    Thread.sleep(20*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public ColaChainWithdrawResponse withdraw(String coinCode, String address, BigDecimal number, String memo) throws Throwable {
        ColaChainWithdrawResponse response = new ColaChainWithdrawResponse();
        ColaChainKey colaChainKey = keyMapper.selectByPrimaryKey(getModuleName());
        String key = colaChainKey.getKey();
        String password = AESUtil.decrypt(key,encoderKey);
        SendResult send = TeraCore.send(TeraCore.accountId, password, Long.valueOf(address), number);
        if (send.getResult() != 0 ){
            response.setSuccess(true);
            response.setFee(BigDecimal.ZERO);
            response.setFeeCoinCode(getModuleName());
            response.setHash(send.getTxID());
        } else {
            response.setSuccess(false);
            response.setErrMessage("错误:"+send.getText());
        }
        return response;
    }


    @Override
    @Scheduled(cron = "0/30 * * * * ?")
    public void confirm() throws Throwable {
        Iterator<String> iterator = unConfirm.keySet().iterator();
        while (iterator.hasNext()){
            String hash = iterator.next();
            ColaChainDepositResponse deposit = depositMapper.selectByPrimaryKey(hash);
            if (System.currentTimeMillis()-deposit.getTimestamp()>60*1000){
                boolean hs = TeraCore.hashSuccess(hash);
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
    public ColaChainBalance getChainBalance(String coinCode, String feeCoinCode) {
        ColaChainBalance chainBalance = new ColaChainBalance();
        chainBalance.setCoinCode(coinCode);
        chainBalance.setFeeCoinCode(feeCoinCode);
        BigDecimal balance = TeraCore.getBalance(TeraCore.accountId);
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

    /**
     * 转移到热钱包
     * @throws Exception
     */
    @Scheduled(cron = "30 0 5 * * ?")
    public void transfer(){
        Set<String> accounts = new HashSet<>();
        List<ColaChainDepositResponse> records = depositMapper.getDepositOrder(getModuleName(),System.currentTimeMillis()-(30*24*60*60*1000L),System.currentTimeMillis());
        for (ColaChainDepositResponse record : records) {
            accounts.add(record.getToAddress());
        }
        log.info("TERA 转移热钱包开始");
        for (String account : accounts) {
            long accountId = Long.valueOf(account);
            // 查询钱包是否有钱,有就转移到热钱包
            BigDecimal balance = TeraCore.getBalance(accountId);
            if (balance.compareTo(BigDecimal.ZERO)>0){
                log.info("转移中:"+account+" 余额:"+balance);
                ColaChainTeraKey colaChainTeraKey = teraKeyMapper.selectByPrimaryKey(account);
                String privateKeyEncode = colaChainTeraKey.getPrivateKey();
                String privateKey = AESUtil.decrypt(privateKeyEncode, encoderKey);
                TeraCore.send(accountId,privateKey,TeraCore.accountId,balance);
            }
        }
        log.info("TERA 转移热钱包结束");
    }

}
