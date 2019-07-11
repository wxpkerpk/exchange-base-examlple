package com.bitcola.chain.server.eos;

import com.bitcola.chain.chain.eos.EosCore;
import com.bitcola.chain.chain.eos.entity.EosTransaction;
import com.bitcola.chain.config.SpringContextsUtil;
import com.bitcola.chain.constant.DepositStatusConstant;
import com.bitcola.chain.entity.ColaChainCoin;
import com.bitcola.chain.entity.ColaChainEosToken;
import com.bitcola.chain.entity.ColaChainKey;
import com.bitcola.chain.mapper.ColaChainCoinMapper;
import com.bitcola.chain.mapper.ColaChainDepositMapper;
import com.bitcola.chain.mapper.ColaChainEosTokenMapper;
import com.bitcola.chain.mapper.ColaChainKeyMapper;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author zkq
 * @create 2019-01-24 18:15
 **/
@Component
@Log4j2
public class EosChainServer extends BaseChainServer {

    @Autowired
    ColaChainEosTokenMapper eosTokenMapper;

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
        scheduler.scheduleWithFixedDelay(()->{
            try {
                List<EosTransaction> transaction = EosCore.getTransaction(EosCore.ADDRESS, 1, 20);
                for (EosTransaction eosTransaction : transaction) {
                    if (eosTransaction.getReceiver().equalsIgnoreCase(EosCore.ADDRESS) &&
                            StringUtils.isNotBlank(eosTransaction.getMemo())){
                        if ((!dev && MemoUtil.isProdMemo(eosTransaction.getMemo())) ||
                                (dev&&MemoUtil.isDevMemo(eosTransaction.getMemo()))){
                            String hash = eosTransaction.getTrx_id();
                            boolean exists = depositMapper.existsWithPrimaryKey(hash);
                            BigDecimal number = new BigDecimal(eosTransaction.getQuantity());
                            String tokenName = eosTransaction.getCode();
                            String coinCode = getCoinCodeByToken(tokenName);
                            if (!exists && number.compareTo(getDepositMin(coinCode))>=0){
                                // 根据 tokenName 获取 coinCode
                                if (StringUtils.isNotBlank(coinCode)){
                                    log.info("扫描到 EOS 交易: "+hash);
                                    ColaChainDepositResponse deposit = new ColaChainDepositResponse();
                                    deposit.setHash(hash);
                                    deposit.setAmount(number);
                                    deposit.setTimestamp(System.currentTimeMillis());
                                    deposit.setModule(getModuleName());
                                    deposit.setCoinCode(coinCode);
                                    deposit.setToAddress(eosTransaction.getReceiver());
                                    deposit.setStatus(DepositStatusConstant.NOT_RECORD);
                                    deposit.setMemo(eosTransaction.getMemo());
                                    String orderId = deposit(deposit);
                                    if (StringUtils.isNotBlank(orderId)){
                                        unConfirm.put(hash,0);
                                    }
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
        return "EOS";
    }

    @Override
    public String newAccount(String coinCode) throws Throwable {
        address.add(EosCore.ADDRESS);
        return EosCore.ADDRESS;
    }

    @Override
    public boolean checkAddress(String address) {
        return EosCore.checkAddress(address);
    }

    @Override
    public ColaChainWithdrawResponse withdraw(String coinCode, String address, BigDecimal number, String memo) throws Throwable {
        ColaChainEosToken token = eosTokenMapper.selectByPrimaryKey(coinCode);
        ColaChainKey colaChainKey = keyMapper.selectByPrimaryKey(getModuleName());
        String key = colaChainKey.getKey();
        String password = AESUtil.decrypt(key,encoderKey);
        ColaChainWithdrawResponse res = EosCore.withdraw(address, memo, token.getSymbol(), number, token.getTokenName(), token.getPrecision(), password);
        res.setFeeCoinCode(getModuleName());
        res.setFee(BigDecimal.ZERO);
        return res;
    }


    @Override
    @Scheduled(cron = "0/10 * * * * ?")
    public void confirm() throws Throwable {
        Iterator<String> iterator = unConfirm.keySet().iterator();
        while (iterator.hasNext()){
            String hash = iterator.next();
            ColaChainDepositResponse deposit = depositMapper.selectByPrimaryKey(hash);
            if (System.currentTimeMillis()-deposit.getTimestamp()>180*1000){
                boolean hs = EosCore.hashSuccess(hash);
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
        ColaChainEosToken eosToken = eosTokenMapper.selectByPrimaryKey(coinCode);
        BigDecimal balance = EosCore.getBalance(eosToken.getTokenName(),EosCore.ADDRESS,eosToken.getSymbol());
        if (eosToken.getCoinCode().equals(feeCoinCode)){
            chainBalance.setBalance(balance);
            chainBalance.setFeeBalance(balance);
        } else {
            chainBalance.setBalance(balance);
            ColaChainEosToken eos = eosTokenMapper.selectByPrimaryKey(getModuleName());
            BigDecimal eosBalance = EosCore.getBalance(eos.getTokenName(), EosCore.ADDRESS, eos.getSymbol());
            chainBalance.setFeeBalance(eosBalance);
        }
        return chainBalance;
    }

    private static final Map<String, String> TOKEN_CACHE = new ConcurrentHashMap<>();
    private String getCoinCodeByToken(String tokenName){
        return TOKEN_CACHE.computeIfAbsent(tokenName,k ->eosTokenMapper.getCoinCodeByTokenName(tokenName));
    }
}
