package com.bitcola.exchange.service;

import com.bitcola.exchange.constant.OrderDirection;
import com.bitcola.exchange.data.MakeOrderParams;
import com.bitcola.exchange.data.RushParams;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * 抢购
 *
 * @author zkq
 * @create 2019-04-22 14:04
 **/
@Service
@Data
public class RushService {
    Map<String, BigDecimal> RUSH_LIMIT = new ConcurrentHashMap<>();
    RushParams params = new RushParams();

    public long getRushStartTime(){
        return params.getRushTimestampStart().get(getRushRound()-1);
    }
    public long getRushEndTime(){
        return params.getRushTimestampEnd().get(getRushRound()-1);
    }
    public BigDecimal getCurrentRushPrice(){
        return params.getRushPrice().get(getRushRound()-1);
    }
    public int getRushRound(){
        if (StringUtils.isNotBlank(params.getRushPair())){
            List<Long> ends = params.getRushTimestampEnd();
            long currentTimeMillis = System.currentTimeMillis();
            for (int i = 0; i < ends.size(); i++) {
                if (ends.get(i) > currentTimeMillis) return i+1;
            }
            return ends.size();
        }
        return 0;
    }

    /**
     * 检测是否需要限制交易
     * @param makeOrderParams
     * @return
     */
    public boolean isPassPairOpenTimeLimit(MakeOrderParams makeOrderParams) {
        if (!BaseContextHandler.getUserID().equals(params.getRushProjectUserId())) return true;
        if (!makeOrderParams.getPair().equals(params.getRushPair())) return true;
        if (makeOrderParams.getType().equals(OrderDirection.BUY)) return true;
        return false;
    }

    /**
     * 是否超出最大限制
     * @param buyNumber
     * @param userId
     * @return
     */
    public boolean isOutOfMaxLimit(BigDecimal buyNumber, String userId){
        if (params.getRushPair() == null ||params.getRushMaxLimit()==null || params.getRushMaxLimit().compareTo(BigDecimal.ZERO)==0) return false;
        BigDecimal alreadyBuy = RUSH_LIMIT.computeIfAbsent(userId, k -> BigDecimal.ZERO);
        return alreadyBuy.add(buyNumber).compareTo(params.getRushMaxLimit()) > 0;
    }

    /**
     * 放入限制
     * @param buyNumber
     * @param userId
     */
    public void putRushLimit(BigDecimal buyNumber,String userId){
        BigDecimal alreadyBuy = RUSH_LIMIT.computeIfAbsent(userId, k -> BigDecimal.ZERO);
        RUSH_LIMIT.put(userId,alreadyBuy.add(buyNumber));
    }


    public void init(RushParams params){
        this.params = params;
        this.RUSH_LIMIT = new ConcurrentHashMap<>();
    }

    public void stop() {
        this.params = new RushParams();
        this.RUSH_LIMIT = new ConcurrentHashMap<>();
    }


    public String getRushPair() {
        return this.params.getRushPair();
    }

    public String getRushProjectUserId() {
        return this.params.getRushProjectUserId();
    }

    public BigDecimal getRushMaxLimit() {
        return this.params.getRushMaxLimit();
    }
}
