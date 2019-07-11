package com.bitcola.chain.server.usdt;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bitcola.chain.chain.btc.BtcCore;
import com.bitcola.chain.chain.usdt.Balance;
import com.bitcola.chain.chain.usdt.UsdtBalanceEntity;
import com.bitcola.chain.chain.usdt.UsdtCore;
import com.bitcola.chain.constant.DepositStatusConstant;
import com.bitcola.chain.entity.ColaChainKey;
import com.bitcola.chain.mapper.ColaChainDepositMapper;
import com.bitcola.chain.mapper.ColaChainKeyMapper;
import com.bitcola.chain.server.BaseChainServer;
import com.bitcola.chain.util.AESUtil;
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
 * @create 2019-01-29 11:04
 **/
@Log4j2
@Component
public class UsdtChainServer extends BaseChainServer {

    public static final int propertyId = 31;

    @Autowired
    ColaChainDepositMapper depositMapper;

    @Value("${bitcola.chain.password}")
    String encoderKey;

    @Autowired
    ColaChainKeyMapper keyMapper;

    @Override
    protected void run() throws Throwable {
        // 获得扫描地址
        address.addAll(getAddress());
        // 将确认中的订单缓存起来推送
        List<ColaChainDepositResponse> unConfirmDeposit = depositMapper.unConfirm(getModuleName());
        for (ColaChainDepositResponse depositResponse : unConfirmDeposit) {
            unConfirm.put(depositResponse.getHash(),0);
        }
        // 开启查询线程
        scheduler.scheduleWithFixedDelay(()->{
            try {
                String transactions = UsdtCore.getTransactions(20, 0);
                List<JSONObject> array = JSONArray.parseArray(transactions,JSONObject.class);
                for (JSONObject object : array) {
                    boolean ismine = object.getBooleanValue("ismine");
                    int pid = object.getIntValue("propertyid");
                    if (pid == propertyId && ismine && address.contains(object.getString("referenceaddress"))
                            && object.getBooleanValue("valid")){
                        String hash = object.getString("txid");
                        BigDecimal amount = object.getBigDecimal("amount");
                        boolean exists = depositMapper.existsWithPrimaryKey(hash);
                        if (!exists && amount.compareTo(getDepositMin(getModuleName()))>=0){
                            log.info("扫描到 USDT 交易: "+hash);
                            ColaChainDepositResponse deposit = new ColaChainDepositResponse();
                            deposit.setHash(hash);
                            deposit.setAmount(amount);
                            deposit.setTimestamp(System.currentTimeMillis());
                            deposit.setModule(getModuleName());
                            deposit.setCoinCode("USDT");
                            deposit.setToAddress(object.getString("referenceaddress"));
                            deposit.setStatus(DepositStatusConstant.NOT_RECORD);
                            String orderId = deposit(deposit);
                            if (StringUtils.isNotBlank(orderId)){
                                unConfirm.put(hash,0);
                                cachedThreadPool.submit(()->{
                                    ColaChainKey colaChainKey = keyMapper.selectByPrimaryKey(getModuleName());
                                    String key = colaChainKey.getKey();
                                    String password = AESUtil.decrypt(key,encoderKey);
                                    UsdtCore.sum(password);
                                });
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
        return "USDT";
    }

    @Override
    public String newAccount(String coinCode) throws Throwable {
        String account = UsdtCore.newAccount();
        address.add(account);
        return account;
    }

    @Override
    public boolean checkAddress(String address) {
        return BtcCore.checkAddress(address);
    }

    @Override
    public ColaChainWithdrawResponse withdraw(String coinCode, String address, BigDecimal number, String memo) throws Throwable {
        ColaChainKey colaChainKey = keyMapper.selectByPrimaryKey(getModuleName());
        String key = colaChainKey.getKey();
        String password = AESUtil.decrypt(key,encoderKey);
        String hash = UsdtCore.transfer(UsdtCore.redeemAddress,address, number,propertyId,password);
        ColaChainWithdrawResponse response = new ColaChainWithdrawResponse();
        response.setHash(hash);
        response.setSuccess(true);
        response.setFeeCoinCode("BTC");
        BigDecimal bigDecimal = JSONObject.parseObject(UsdtCore.getTransaction(hash)).getBigDecimal("fee");
        if (bigDecimal == null){
            bigDecimal = BigDecimal.ZERO;
        } else {
            bigDecimal = bigDecimal.abs();
        }
        response.setFee(bigDecimal);
        return response;
    }

    @Override
    @Scheduled(cron = "0/10 * * * * ?")
    public void confirm() throws Throwable {
        Iterator<String> iterator = unConfirm.keySet().iterator();
        while (iterator.hasNext()){
            String hash = iterator.next();
            JSONObject result = JSONObject.parseObject(UsdtCore.getTransaction(hash));
            int confirmNumber = result.getIntValue("confirmations");
            ColaChainDepositResponse colaChainDepositResponse = depositMapper.selectByPrimaryKey(hash);
            if (confirmNumber >= getConfirmNumber(getModuleName())){
                colaChainDepositResponse.setStatus(DepositStatusConstant.CONFIRM);
                depositMapper.updateByPrimaryKeySelective(colaChainDepositResponse);
                boolean success = completeDeposit(colaChainDepositResponse.getOrderId());
                iterator.remove();
            } else if (confirmNumber > unConfirm.get(hash)){
                super.confirmNumber(confirmNumber,colaChainDepositResponse.getOrderId());
                unConfirm.put(hash,confirmNumber);
            }
        }
    }

    @Override
    public ColaChainBalance getChainBalance(String coinCode, String feeCoinCode) throws Throwable{
        ColaChainBalance chainBalance = new ColaChainBalance();
        chainBalance.setCoinCode(coinCode);
        chainBalance.setFeeCoinCode(feeCoinCode);
        BigDecimal balance = UsdtCore.getBalance(UsdtCore.fromAddress);
        BigDecimal btcBalance = UsdtCore.getBtcBalance();
        chainBalance.setBalance(balance);
        chainBalance.setFeeBalance(btcBalance);
        return chainBalance;
    }

    /**
     * 转移资产到中央账户(有问题)
     */
    public void transfer() throws Throwable {
        ColaChainKey colaChainKey = keyMapper.selectByPrimaryKey(getModuleName());
        String key = colaChainKey.getKey();
        String password = AESUtil.decrypt(key,encoderKey);
        String addressBalance = UsdtCore.getAddressBalance();
        List<UsdtBalanceEntity> jsonObjects = JSONArray.parseArray(addressBalance, UsdtBalanceEntity.class);
        for (UsdtBalanceEntity balanceEntity : jsonObjects) {
            List<Balance> balances = balanceEntity.getBalances();
            for (Balance balance : balances) {
                int propertyid = balance.getPropertyid();
                if (propertyid == 31 && balance.getBalance().compareTo(BigDecimal.ONE)>=0){
                    UsdtCore.transfer(balanceEntity.getAddress(),UsdtCore.fromAddress,balance.getBalance(),propertyId,password);
                    log.info("转移了一笔 USDT 到中央账户");
                }
            }
        }
    }

    public static void main(String[] args)throws Throwable {
        String transaction = UsdtCore.getTransaction("a319635f92040232830892cbc9dd8c2364425ad5f45bb301d680aa759b4866c2");
        System.out.println(transaction);
    }


}
