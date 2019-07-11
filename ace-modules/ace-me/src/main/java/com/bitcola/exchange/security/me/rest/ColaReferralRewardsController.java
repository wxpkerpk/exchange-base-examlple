package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.Query;
import com.bitcola.exchange.security.me.biz.ColaCoinBiz;
import com.bitcola.exchange.security.me.biz.ColaReferralRewardsBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 邀请奖励
 *
 * @author zkq
 * @create 2018-10-21 14:40
 **/
@RestController
@RequestMapping("referralRewards")
public class ColaReferralRewardsController {

    @Autowired
    ColaReferralRewardsBiz referralRewardsBiz;



    /**
     * 邀请信息
     * @return
     */
    @RequestMapping("getReferralCode")
    public AppResponse getReferralCode(){
        Map<String,Object> map = referralRewardsBiz.getReferralCode();
        return AppResponse.ok().data(map);
    }

    /**
     * 已经邀请的好友
     * @param params
     * @return
     */
    @RequestMapping("inviteFriends")
    public TableResultResponse inviteFriends(@RequestParam Map<String,Object> params){
        TableResultResponse result = referralRewardsBiz.inviteFriends(new Query(params));
        return result;
    }


    /**
     *  已获得的奖励估值
     * @return
     */
    @RequestMapping("referralRewards")
    public AppResponse referralRewards(){
        BigDecimal amount = referralRewardsBiz.referralRewards();
        return AppResponse.ok().data(amount);
    }


}
