package com.bitcola.dataservice.biz;

import com.bitcola.caculate.entity.ExchangeLog;
import com.bitcola.caculate.entity.RewardLog;
import com.bitcola.dataservice.mapper.*;
import com.bitcola.dataservice.util.Snowflake;
import com.bitcola.dataservice.util.SpringContextsUtil;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import com.bitcola.exchange.security.common.constant.SystemBalanceConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RewardBiz extends BaseBiz<RewardMapper, RewardLog> {
    @Resource(name = "rewardPoll")
    private Executor taskAsyncPool;
    Snowflake snowflake=new Snowflake();

    @Autowired
    SpringContextsUtil springContextsUtil;
    @Autowired
    ColaUserBalanceMapper mapper;

    @Autowired
    ColaCaculaterOrderMapper colaCaculaterOrderMapper;

    @Autowired
    ColaCaculateExchangeLogMapper colaCaculateExchangeLogMapper;


    @Autowired
    ColaCoinSymbolMapper colaCoinSymbolMapper;
    @Autowired
    ColaSystemBalanceBiz colaSystemBalanceBiz;

    @Autowired
    ColaConfigBiz colaConfigBiz;

    @Autowired
    ColaUserMapper colaUserMapper;
    @Transactional
    public void insertExchangeLogs(ArrayList<ExchangeLog> exchangeLogs) {

            String reward_fee_factorStr = colaConfigBiz.getConfig("referral_rewards_transaction_fees");
            if (reward_fee_factorStr == null) reward_fee_factorStr = "0";
            BigDecimal reward_fee_factor =new BigDecimal(reward_fee_factorStr);
            var toGroupMap = new HashMap<String, BigDecimal>();
            var fromGroupMap = new HashMap<String, BigDecimal>();
            var toGroupInMap = new HashMap<String, BigDecimal>();
            var fromGroupInMap = new HashMap<String, BigDecimal>();
            String fromFeeCode = null;
            String toFeeCode = null;
            //插入日志
            for (ExchangeLog exchangeLog : exchangeLogs) {
                fromFeeCode = exchangeLog.getFromFeeCoinCode();
                toFeeCode = exchangeLog.getToFeeCoinCode();
                // 加签名
                String invitorUserId = mapper.selectInvitor(exchangeLog.getFromUserId());
                String invitorToUserId = mapper.selectInvitor(exchangeLog.getToUserId());
                if (reward_fee_factor.doubleValue() > 0) {
                    BigDecimal fromRewardFee = exchangeLog.getFromFee().multiply(reward_fee_factor);
                    BigDecimal toRewardFee = exchangeLog.getToFee().multiply(reward_fee_factor);
                    if (invitorUserId != null) {
                        BigDecimal f = fromGroupMap.getOrDefault(invitorUserId, BigDecimal.ZERO);
                        f = f.add(fromRewardFee);
                        fromGroupMap.put(invitorUserId, f);
                        exchangeLog.setFromFee(exchangeLog.getFromFee().subtract( fromRewardFee));
                        insertRewardLog(invitorUserId, fromRewardFee, exchangeLog.getFromFeeCoinCode(), exchangeLog.getFromOrderId());
                    }
                    fromGroupInMap.put("8", fromGroupInMap.getOrDefault(exchangeLog.getFromUserId(), BigDecimal.ZERO).add(exchangeLog.getFromFee()));
                    if (invitorToUserId != null) {
                        exchangeLog.setToFee(exchangeLog.getToFee().subtract(toRewardFee));
                        BigDecimal f = toGroupMap.getOrDefault(invitorToUserId, BigDecimal.ZERO);
                        f = f.add(toRewardFee);
                        toGroupMap.put(invitorToUserId, f);
                        insertRewardLog(invitorToUserId, toRewardFee, exchangeLog.getToFeeCoinCode(), exchangeLog.getToOrderId());
                    }
                    toGroupInMap.put("8",toGroupInMap.getOrDefault(exchangeLog.getToUserId(), BigDecimal.ZERO).add( exchangeLog.getToFee()));

                }
            }
            updateBalanceAndSystemIn(toGroupMap, fromGroupMap, toGroupInMap, fromGroupInMap, fromFeeCode, toFeeCode);
    }

    //合并数据库写入操作
    @Transactional
    public void updateBalanceAndSystemIn(HashMap<String, BigDecimal> toGroupMap, HashMap<String, BigDecimal> fromGroupMap, HashMap<String, BigDecimal> toGroupInMap, HashMap<String, BigDecimal> fromGroupInMap, String fromFeeCode, String toFeeCode) {
        for (String userid : fromGroupMap.keySet()) {
            mapper.addUserBanlance(userid, fromFeeCode, fromGroupMap.get(userid), EncoderUtil.BALANCE_KEY);
        }
        for (String userid : toGroupMap.keySet()) {
            mapper.addUserBanlance(userid, toFeeCode, toGroupMap.get(userid), EncoderUtil.BALANCE_KEY);
        }
        for (String userid : fromGroupInMap.keySet()) {
            colaSystemBalanceBiz.add(userid, fromGroupInMap.get(userid), fromFeeCode, SystemBalanceConstant.FEES_TRANSACTION, "交易手续费");
        }
        for (String userid : toGroupInMap.keySet()) {
            colaSystemBalanceBiz.add(userid,toGroupInMap.get(userid), toFeeCode, SystemBalanceConstant.FEES_TRANSACTION, "交易手续费");
        }
    }

    private void insertRewardLog(String invitorToUserId, BigDecimal toRewardFee, String code, String orderId) {
        RewardLog rewardLog = new RewardLog();
        rewardLog.setId(snowflake.nextIdStr());
        rewardLog.setCoinCode(code);
        rewardLog.setUserId(invitorToUserId);
        rewardLog.setCount(toRewardFee);
        rewardLog.setOrderId(orderId);
        rewardLog.setTime(System.currentTimeMillis());
        insert(rewardLog);

    }

}
