package com.bitcola.dataservice.biz;

import com.bitcola.caculate.entity.CoinChange;
import com.bitcola.caculate.entity.ColaOrder;
import com.bitcola.caculate.entity.ExchangeLog;
import com.bitcola.caculate.entity.RewardLog;
import com.bitcola.config.DataServiceConstant;
import com.bitcola.dataservice.mapper.*;
import com.bitcola.dataservice.util.Snowflake;
import com.bitcola.dataservice.util.SpringContextsUtil;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import com.bitcola.exchange.security.common.constant.OrderStateConstants;
import com.bitcola.exchange.security.common.constant.SystemBalanceConstant;
import com.bitcola.me.entity.ColaCoinSymbol;
import com.bitcola.me.entity.ColaMeBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 用户资金
 *
 * @author zkq
 * @create 2018-07-14 14:18
 **/
@Service(value = "colaUserBalanceBiz")
public class ColaUserBalanceBiz extends BaseBiz<ColaUserBalanceMapper, ColaMeBalance> {

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
    @Autowired
    RewardMapper rewardMapper;

    @Autowired
            RewardBiz rewardBiz;

    Snowflake snowflake=new Snowflake();

    /**
     * 获取用户币种余额
     *
     * @param userID
     * @param coinCode
     * @return
     */
    public BigDecimal getCoinNumber(String userID, String coinCode) {
        return mapper.getCoinNumber(userID, coinCode);
    }


    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ColaOrder makeOrder(String userId, String code, BigDecimal price, BigDecimal count, BigDecimal total, String type) throws Exception {
        CoinChange coinChange = new CoinChange();

        String codes[] = code.split("_");
        String fromCode = null;
        String toCode;
        ColaOrder colaOrder = new ColaOrder();

        switch (type) {
            case "buy": {
                fromCode = codes[1];
                colaOrder.setOriginTotal(total);
                colaOrder.setTotal(total);

                break;
            }
            case "sell": {
                total = count;
                colaOrder.setTotal(count.multiply(price));
                colaOrder.setOriginTotal(count);
                fromCode = codes[0];
                break;
            }
        }
        coinChange.setCoinCode(fromCode);
        coinChange.setGain(total);
        coinChange.setUserID(userId);
        coinChange.setType(DataServiceConstant.OPERATION.REDUCE_BANLANCE);
        int tag = mapper.reduceUserBanlance(userId, fromCode, total, EncoderUtil.BALANCE_KEY);
        if (tag == 0) throw new Exception("用户余额不足");
        coinChange.setType(DataServiceConstant.OPERATION.ADD_FROZEN);
        tag = mapper.addUserFrozenBanlance(userId, fromCode, total, EncoderUtil.BALANCE_KEY);
        colaOrder.setTime(System.currentTimeMillis());
        colaOrder.setCount(count);
        colaOrder.setPrice(price);
        colaOrder.setType(type);
        colaOrder.setCoinCode(code);
        colaOrder.setId(snowflake.nextIdStr());
        colaOrder.setUserId(userId);
        colaOrder.setStatus(OrderStateConstants.Pending);
        colaCaculaterOrderMapper.insert(colaOrder);
        return colaOrder;

    }


    @Transactional
    public boolean transformBalance(String userId, String from, String to, BigDecimal fromCount, BigDecimal toCount) {
        CoinChange coinChange = new CoinChange();
        coinChange.setCoinCode(from);
        coinChange.setUserID(userId);
        coinChange.setGain(fromCount);
        int tag=  mapper.reduceUserFrozenBanlance(userId,from,fromCount,EncoderUtil.BALANCE_KEY);
        if(tag==0) return false;
        coinChange.setCoinCode(to);
        coinChange.setGain(toCount);
        mapper.addUserBanlance(userId, to, toCount, EncoderUtil.BALANCE_KEY);
        return true;

    }



    public String getMoneyPassword(String userId) {
        return colaUserMapper.getMoneyPassword(userId);

    }


    @Transactional
    public int  forceReduceFrozenBalance(String userId,String coinCode,BigDecimal amount)
    {
        int tag=  mapper.reduceUserFrozenBanlance(userId,coinCode,amount,EncoderUtil.BALANCE_KEY);
        if(tag==0) tag= mapper.setFrozenBanlanceZero(userId,coinCode,EncoderUtil.BALANCE_KEY);
        return tag;

    }


}
