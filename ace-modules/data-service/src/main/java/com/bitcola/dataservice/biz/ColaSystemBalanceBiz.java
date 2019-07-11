package com.bitcola.dataservice.biz;

import com.bitcola.caculate.entity.CoinChange;
import com.bitcola.dataservice.mapper.ColaSystemBalanceMapper;
import com.bitcola.dataservice.mapper.ColaUserBalanceMapper;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import com.bitcola.exchange.security.common.constant.UserConstant;
import com.bitcola.exchange.security.common.util.TimeUtils;
import com.bitcola.me.entity.ColaSystemBalance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @author zkq
 * @create 2018-10-25 16:04
 **/
@Service
public class ColaSystemBalanceBiz extends BaseBiz<ColaSystemBalanceMapper,ColaSystemBalance> {

    @Autowired
    ColaUserBalanceMapper balanceMapper;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void in(ColaSystemBalance balance){
        balance.setId(UUID.randomUUID().toString());
        balance.setTime(System.currentTimeMillis());
        balanceMapper.systemIn(balance, EncoderUtil.BALANCE_KEY);

    }


    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void out(ColaSystemBalance balance){
        balance.setId(UUID.randomUUID().toString());
        balance.setTime(System.currentTimeMillis());
        int i = balanceMapper.systemOut(balance,EncoderUtil.BALANCE_KEY);
        if (i == 0){
            throw new RuntimeException("余额不足");
        }
        mapper.insert(balance);
    }


    @Transactional
    public ColaSystemBalance add(String userId, BigDecimal amount, String coinCode, String type, String description) {
        ColaSystemBalance balance = new ColaSystemBalance();
        balance.setFromUser(userId);
        balance.setToUser("8");
        balance.setCoinCode(coinCode);
        balance.setAmount(amount);
        balance.setType(type);
        balance.setDescription(description);
        balance.setAction("in");
        in(balance);
        return balance;
    }

    public boolean in(String userId, BigDecimal amount, String coinCode, String type, String description){
        var balance=add(userId,  amount,  coinCode,  type,  description);
        mapper.insert(balance);
        return true;
    }

    /**
     * 所有字段不允许为空
     * @param userId 给哪个发钱
     * @param amount 数量
     * @param coinCode 币种
     * @param type  交易类型
     * @param description 描述
     * @return
     */
    public boolean out(String userId, BigDecimal amount,String coinCode,String type,String description){
        if (StringUtils.isAnyBlank(userId,coinCode,type,description)){
            return false;
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            return false;
        }
        ColaSystemBalance balance = new ColaSystemBalance();
        balance.setToUser(userId);
        balance.setFromUser("8");
        balance.setCoinCode(coinCode);
        balance.setAmount(amount);
        balance.setType(type);
        balance.setDescription(description);
        balance.setAction("out");
        try {
            out(balance);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean transformBalance(String fromUser, String toUser, String coinCode, boolean fromFrozen, boolean toFrozen, BigDecimal number, String type,String description) {
        int i = 1;
        try {
            if (fromFrozen){
                if (toFrozen){
                    changeBalance(fromUser, toUser, coinCode, number);
                    i = balanceMapper.addUserFrozenBanlance(toUser,coinCode,number,EncoderUtil.BALANCE_KEY);
                    if (i==0) throw new RuntimeException("资金异常: "+toUser+" ,当前时间 :"+ TimeUtils.getDateFormat(System.currentTimeMillis()));
                } else {
                    changeBalance(fromUser, toUser, coinCode, number);
                    i = balanceMapper.addUserBanlance(toUser,coinCode,number,EncoderUtil.BALANCE_KEY);
                    if (i==0) throw new RuntimeException("资金异常: "+toUser+" ,当前时间 :"+ TimeUtils.getDateFormat(System.currentTimeMillis()));
                }
            } else {
                if (toFrozen){
                    changeBalances(fromUser, toUser, coinCode, number);
                    i = balanceMapper.addUserFrozenBanlance(toUser,coinCode,number,EncoderUtil.BALANCE_KEY);
                    if (i==0) throw new RuntimeException("资金异常: "+toUser+" ,当前时间 :"+ TimeUtils.getDateFormat(System.currentTimeMillis()));
                } else {
                    changeBalances(fromUser, toUser, coinCode, number);
                    i = balanceMapper.addUserBanlance(toUser,coinCode,number,EncoderUtil.BALANCE_KEY);
                    if (i==0) throw new RuntimeException("资金异常: "+toUser+" ,当前时间 :"+ TimeUtils.getDateFormat(System.currentTimeMillis()));
                }
            }
            if (UserConstant.SYS_ACCOUNT_ID.equals(fromUser) || UserConstant.SYS_ACCOUNT_ID.equals(toUser)){
                ColaSystemBalance balance = new ColaSystemBalance();
                balance.setFromUser(fromUser);
                balance.setToUser(toUser);
                balance.setCoinCode(coinCode);
                balance.setAmount(number);
                balance.setType(type);
                balance.setDescription(description);
                if (UserConstant.SYS_ACCOUNT_ID.equals(fromUser)){
                    balance.setAction("out");
                } else {
                    balance.setAction("in");
                }
                balance.setId(UUID.randomUUID().toString());
                balance.setTime(System.currentTimeMillis());
                mapper.insert(balance);
            }
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void changeBalances(String fromUser, String toUser, String coinCode, BigDecimal number) {
        int i;
        CoinChange coinChange = new CoinChange();
        coinChange.setCoinCode(coinCode);
        coinChange.setUserID(fromUser);
        coinChange.setGain(number);
        i = balanceMapper.reduceUserBanlance(fromUser,coinCode,number, EncoderUtil.BALANCE_KEY);
        if (i==0) throw new RuntimeException("资金异常: "+fromUser+" ,当前时间 :"+ TimeUtils.getDateFormat(System.currentTimeMillis()));
        coinChange.setUserID(toUser);
    }

    public void changeBalance(String fromUser, String toUser, String coinCode, BigDecimal number) {
        int i;
        CoinChange coinChange = new CoinChange();
        coinChange.setCoinCode(coinCode);
        coinChange.setUserID(fromUser);
        coinChange.setGain(number);
        i = balanceMapper.reduceUserFrozenBanlance(fromUser,coinCode,number, EncoderUtil.BALANCE_KEY);
        if (i==0) throw new RuntimeException("资金异常: "+fromUser+" ,当前时间 :"+ TimeUtils.getDateFormat(System.currentTimeMillis()));
        coinChange.setUserID(toUser);
    }
}
