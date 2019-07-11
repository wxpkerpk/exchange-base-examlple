package com.bitcola.chain.server.btc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bitcola.chain.chain.btc.BtcCore;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author zkq
 * @create 2019-01-21 15:01
 **/
@Log4j2
@Component
public class BtcChainServer extends BaseChainServer {


    @Autowired
    ColaChainDepositMapper depositMapper;

    @Value("${bitcola.chain.password}")
    String encoderKey;

    @Autowired
    ColaChainKeyMapper keyMapper;


    @Override
    protected void run() throws Throwable{
        // 获得扫描地址
        address.addAll(getAddress());
        // 将确认中的订单缓存起来推送
        List<ColaChainDepositResponse> unConfirmDeposit = depositMapper.unConfirm(getModuleName());
        for (ColaChainDepositResponse depositResponse : unConfirmDeposit) {
            unConfirm.put(depositResponse.getHash(),0);
        }
        BigDecimal depositMin = getDepositMin(getModuleName());
        // 开启查询线程
        scheduler.scheduleWithFixedDelay(()->{
            try {
                Object transactions = BtcCore.getTransactions(20, 0);
                List<JSONObject> array = JSONArray.parseArray(JSONObject.toJSONString(transactions),JSONObject.class);
                for (JSONObject object : array) {
                    String category = object.getString("category");
                    if ("receive".equalsIgnoreCase(category) && address.contains(object.getString("address"))
                            && depositMin.compareTo(object.getBigDecimal("amount")) <= 0){
                        String hash = object.getString("txid");
                        boolean exists = depositMapper.existsWithPrimaryKey(hash);
                        if (!exists){
                            log.info("扫描到 BTC 交易: "+hash);
                            ColaChainDepositResponse deposit = new ColaChainDepositResponse();
                            deposit.setHash(hash);
                            deposit.setAmount(object.getBigDecimal("amount"));
                            deposit.setTimestamp(System.currentTimeMillis());
                            deposit.setModule(getModuleName());
                            deposit.setCoinCode("BTC");
                            deposit.setToAddress(object.getString("address"));
                            deposit.setStatus(DepositStatusConstant.NOT_RECORD);
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
        return "BTC";
    }

    @Override
    public String newAccount(String coinCode) throws Throwable {
        String newAddress = BtcCore.getNewAddress();
        address.add(newAddress);
        return newAddress;
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
        String hash = BtcCore.sendtoaddress(address, number,password);
        ColaChainWithdrawResponse response = new ColaChainWithdrawResponse();
        response.setHash(hash);
        response.setSuccess(true);
        response.setFeeCoinCode("BTC");
        BigDecimal bigDecimal = JSONObject.parseObject(JSONObject.toJSONString(BtcCore.getTransaction(hash))).getBigDecimal("fee");
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
            JSONObject result = JSONObject.parseObject(JSONObject.toJSONString(BtcCore.getTransaction(hash)));
            int confirmNumber = result.getIntValue("confirmations");
            if (confirmNumber >= super.getConfirmNumber(getModuleName())){
                ColaChainDepositResponse colaChainDepositResponse = depositMapper.selectByPrimaryKey(hash);
                colaChainDepositResponse.setStatus(DepositStatusConstant.CONFIRM);
                depositMapper.updateByPrimaryKeySelective(colaChainDepositResponse);
                iterator.remove();
                completeDeposit(colaChainDepositResponse.getOrderId());
            } else {
                unConfirm.put(hash,confirmNumber);
            }
        }
    }

    @Override
    public ColaChainBalance getChainBalance(String coinCode, String feeCoinCode) throws Throwable{
        ColaChainBalance chainBalance = new ColaChainBalance();
        chainBalance.setCoinCode(coinCode);
        chainBalance.setFeeCoinCode(feeCoinCode);
        BigDecimal balance = new BigDecimal(BtcCore.getBalance());
        chainBalance.setBalance(balance);
        chainBalance.setFeeBalance(balance);
        return chainBalance;
    }


}
