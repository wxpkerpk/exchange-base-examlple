package com.bitcola.exchange.security.me.biz;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.Query;
import com.bitcola.exchange.security.me.feign.IConfigFeign;
import com.bitcola.exchange.security.me.mapper.ColaReferralRewardsMapper;
import com.bitcola.exchange.security.me.vo.InvitationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 邀请奖励
 *
 * @author zkq
 * @create 2018-10-21 14:47
 **/
@Service
public class ColaReferralRewardsBiz {

    @Autowired
    ColaReferralRewardsMapper referralRewardsMapper;

    @Autowired
    IConfigFeign configFeign;
    @Autowired
    ColaCoinBiz colaCoinBiz;


    public Map<String, Object> getReferralCode() {
        Map<String, Object> map = new HashMap<>();
        String invitationCode = referralRewardsMapper.getReferralCode(BaseContextHandler.getUserID());
        map.put("invitationCode",invitationCode);
        map.put("transactionFees",getTransactionFees());
        return map;
    }


    @Cached(key = "getTransactionFees", cacheType = CacheType.LOCAL, expire = 10)
    public BigDecimal getTransactionFees(){
        return new BigDecimal(configFeign.getConfig("referral_rewards_transaction_fees"));
    }

    public TableResultResponse inviteFriends(Query query) {
        String invitationCode = referralRewardsMapper.getReferralCode(BaseContextHandler.getUserID());
        query.put("invitationCode", invitationCode);
        Long total = referralRewardsMapper.countInviteFriends(invitationCode);
        List<InvitationVo> list = referralRewardsMapper.listInviteFriends(invitationCode,query.getLimit(),query.getPage());
        return new TableResultResponse(total,list);
    }

    public BigDecimal referralRewards() {
        List<Map<String,Object>> list = referralRewardsMapper.referralRewards(BaseContextHandler.getUserID());
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> map : list) {
            String coin = map.get("coin").toString();
            BigDecimal amount = (BigDecimal)map.get("amount");
            total = total.add(amount.multiply(colaCoinBiz.getCoinWorth(coin)));
        }
        total = total.setScale(2, RoundingMode.DOWN);
        return total;
    }
}
