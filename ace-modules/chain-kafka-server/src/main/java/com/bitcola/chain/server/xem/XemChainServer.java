package com.bitcola.chain.server.xem;

import com.bitcola.chain.chain.stellar.StellarCore;
import com.bitcola.chain.chain.usdt.UsdtCore;
import com.bitcola.chain.chain.xem.XemCore;
import com.bitcola.chain.chain.xem.entity.XemTransactionLog;
import com.bitcola.chain.constant.DepositStatusConstant;
import com.bitcola.chain.entity.ColaChainKey;
import com.bitcola.chain.entity.ColaChainXemToken;
import com.bitcola.chain.mapper.ColaChainDepositMapper;
import com.bitcola.chain.mapper.ColaChainKeyMapper;
import com.bitcola.chain.mapper.ColaChainXemTokenMapper;
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
 * @create 2019-01-25 15:33
 **/
@Component
@Log4j2
public class XemChainServer extends BaseChainServer {

    @Autowired
    ColaChainDepositMapper depositMapper;

    @Autowired
    ColaChainKeyMapper keyMapper;

    @Autowired
    ColaChainXemTokenMapper xemTokenMapper;

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
        scheduler.scheduleWithFixedDelay(()->{
            try {
                List<XemTransactionLog> transaction = XemCore.transfersIncoming(XemCore.ADDRESS, null, null);
                for (XemTransactionLog trans : transaction) {
                    if ((!dev && MemoUtil.isProdMemo(trans.getMemo())) ||
                            (dev&&MemoUtil.isDevMemo(trans.getMemo()))){
                        boolean exists = depositMapper.existsWithPrimaryKey(trans.getTxId());
                        if (!exists){
                            boolean token = trans.isToken();
                            String coinCode;
                            if (token){
                                String mosaicIdString = trans.getMosaicIdString();
                                String[] split = mosaicIdString.split(":");
                                coinCode = xemTokenMapper.getCoinCodeByTokenName(split[1], split[0]);
                            } else {
                                coinCode = getModuleName();
                            }
                            BigDecimal number = trans.getNumber();
                            if (StringUtils.isNotBlank(coinCode) && number.compareTo(getDepositMin(coinCode)) >= 0){
                                ColaChainDepositResponse deposit = new ColaChainDepositResponse();
                                deposit.setHash(trans.getTxId());
                                deposit.setAmount(number);
                                deposit.setTimestamp(System.currentTimeMillis());
                                deposit.setModule(getModuleName());
                                deposit.setCoinCode(coinCode);
                                deposit.setToAddress(XemCore.ADDRESS);
                                deposit.setStatus(DepositStatusConstant.NOT_RECORD);
                                deposit.setMemo(trans.getMemo());
                                String orderId = deposit(deposit);
                                if (StringUtils.isNotBlank(orderId)){
                                    unConfirm.put(trans.getTxId(),0);
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
        return "XEM";
    }

    @Override
    public String newAccount(String coinCode) throws Throwable {
        String account = XemCore.newAccount();
        address.add(account);
        return account;
    }

    @Override
    public boolean checkAddress(String address) {
        return XemCore.checkAddress(address);
    }

    @Override
    public ColaChainWithdrawResponse withdraw(String coinCode, String address, BigDecimal number, String memo) throws Throwable {
        ColaChainKey colaChainKey = keyMapper.selectByPrimaryKey(getModuleName());
        String key = colaChainKey.getKey();
        String password = AESUtil.decrypt(key,encoderKey);
        ColaChainWithdrawResponse response;
        if (getModuleName().equalsIgnoreCase(coinCode)){
            response = XemCore.withdraw(address,memo,number,null,password);
        } else {
            ColaChainXemToken token = xemTokenMapper.selectByPrimaryKey(coinCode);
            String mosaicIdString = token.getNamespaceId()+":"+token.getTokenName();
            response = XemCore.withdraw(address,memo,number,mosaicIdString,password);
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
                boolean hs = XemCore.hashSuccess(hash);
                if (hs){
                    boolean success = completeDeposit(deposit.getOrderId());
                    iterator.remove();
                    deposit.setStatus(DepositStatusConstant.CONFIRM);
                    depositMapper.updateByPrimaryKeySelective(deposit);
                }
            }
        }
    }

    @Override
    public ColaChainBalance getChainBalance(String coinCode, String feeCoinCode) throws Exception{
        ColaChainBalance chainBalance = new ColaChainBalance();
        chainBalance.setCoinCode(coinCode);
        chainBalance.setFeeCoinCode(feeCoinCode);
        BigDecimal xemBalance = XemCore.getXemBalance();
        chainBalance.setFeeBalance(xemBalance);
        if (coinCode.equals(feeCoinCode)){
            chainBalance.setBalance(xemBalance);
        } else {
            ColaChainXemToken token = xemTokenMapper.selectByPrimaryKey(coinCode);
            String mosaicIdString = token.getNamespaceId()+":"+token.getTokenName();
            BigDecimal mosaicBalance = XemCore.getMosaicBalance(mosaicIdString);
            chainBalance.setBalance(mosaicBalance);
        }
        return chainBalance;
    }
}
