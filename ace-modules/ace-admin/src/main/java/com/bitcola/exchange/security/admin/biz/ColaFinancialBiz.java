package com.bitcola.exchange.security.admin.biz;

import com.bitcola.exchange.security.admin.entity.ColaUserReward;
import com.bitcola.exchange.security.admin.entity.ColaVirtualAsset;
import com.bitcola.exchange.security.admin.feign.IDataServiceFeign;
import com.bitcola.exchange.security.admin.mapper.ColaFinancialMapper;
import com.bitcola.exchange.security.admin.mapper.ColaUserRewardMapper;
import com.bitcola.exchange.security.admin.mapper.ColaVirtualAssetMapper;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.constant.FinancialConstant;
import com.bitcola.exchange.security.common.constant.SystemBalanceConstant;
import com.bitcola.exchange.security.common.constant.UserConstant;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.AdminQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author zkq
 * @create 2018-11-28 16:55
 **/
@Service
public class ColaFinancialBiz {

    @Autowired
    IDataServiceFeign dataServiceFeign;

    @Autowired
    ColaUserRewardMapper userRewardMapper;

    @Autowired
    ColaFinancialMapper financialMapper;

    @Autowired
    ColaVirtualAssetMapper virtualAssetMapper;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean reward(String coinCode, BigDecimal number, String userId,String description) {
        // 系统扣钱,用户加钱,记录日志
        boolean b = dataServiceFeign.transformBalance(UserConstant.SYS_ACCOUNT_ID, userId, coinCode, false, false, number, SystemBalanceConstant.REWARD_SYSTEM, description);
        if (!b) return false;
        ColaUserReward userReward = new ColaUserReward();
        userReward.setId(UUID.randomUUID().toString());
        userReward.setAccount(number);
        userReward.setCoinCode(coinCode);
        userReward.setTime(System.currentTimeMillis());
        userReward.setUserId(userId);
        userReward.setStatus("Completed");
        userReward.setActionType(FinancialConstant.SYSTEM_REWARD);
        userReward.setDescription(description);
        userRewardMapper.insertSelective(userReward);
        return true;
    }

    public TableResultResponse page(AdminQuery query) {
        Long total = financialMapper.total(query);
        List<Map<String,Object>> list = financialMapper.page(query);
        return new TableResultResponse(total,list);
    }

    public boolean freeze(String coinCode, BigDecimal amount, String userId, String description) {
        boolean b = dataServiceFeign.transformBalance(userId, userId, coinCode, false, true, amount, SystemBalanceConstant.FROZEN_SYSTEM, description);
        return b;
    }

    public boolean unFrozen(String coinCode, BigDecimal amount, String userId, String description) {
        boolean b = dataServiceFeign.transformBalance(userId, userId, coinCode, true, false, amount, SystemBalanceConstant.UNFROZEN_SYSTEM, description);
        return b;
    }

    public boolean reduce(String coinCode, BigDecimal amount, String userId, String description) {
        boolean b = dataServiceFeign.transformBalance(userId, UserConstant.SYS_ACCOUNT_ID, coinCode, false, false, amount, SystemBalanceConstant.REDUCE_SYSTEM, description);
        return b;
    }

    public void addVirtualAsset(String coinCode, String number, String description) {
        ColaVirtualAsset asset = new ColaVirtualAsset();
        BigDecimal amount = new BigDecimal(number);
        asset.setId(UUID.randomUUID().toString());
        asset.setTimestamp(System.currentTimeMillis());
        asset.setToUser("8");
        asset.setCoinCode(coinCode);
        asset.setNumber(amount);
        asset.setDescription(description);
        virtualAssetMapper.insertSelective(asset);
        virtualAssetMapper.addVirtualAsset(amount,coinCode,EncoderUtil.BALANCE_KEY);

    }

    // id, 昵称,用户名,手机,邮箱,资产
    public TableResultResponse coinRange(AdminQuery query) {
        Long total = financialMapper.countUser(query);
        List<Map<String,Object>> list = financialMapper.coinRange(query);
        return new TableResultResponse(total,list);
    }

    public TableResultResponse financialPage(AdminQuery query) {
        List<Map<String,Object>> list = financialMapper.financialPage(query);
        Long total = financialMapper.financialCount(query);
        return new TableResultResponse(total,list);
    }
}
