package com.bitcola.activity.biz;

import com.bitcola.activity.mapper.SignUpMapper;
import com.bitcola.activity.entity.SignUp;
import com.bitcola.activity.feign.IDataServiceFeign;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import com.bitcola.exchange.security.common.constant.SystemBalanceConstant;
import com.bitcola.exchange.security.common.constant.UserConstant;
import com.bitcola.me.entity.ColaUserEntity;
import com.bitcola.me.entity.ColaUserKyc;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * @author zkq
 * @create 2018-11-29 12:33
 **/
@Service
public class SignUpBiz extends BaseBiz<SignUpMapper, SignUp> {

    public static final BigDecimal NEW_USER = new BigDecimal(20);
    public static final BigDecimal INVITER = new BigDecimal(10);
    public static final BigDecimal ALCT_REWARD = new BigDecimal(15);

    @Autowired
    IDataServiceFeign dataServiceFeign;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean reward(String userId) {
        alctReward(userId);

        SignUp signUp = new SignUp();
        signUp.setUserId(userId);
        signUp.setIsInviterReward(0);
        List<SignUp> select = mapper.select(signUp);
        BigDecimal total = mapper.total();
        if (total == null) {
            total = BigDecimal.ZERO;
        }
        if (select.size()==0 && total.compareTo(new BigDecimal(10000000))<=0){
            BigDecimal reward = NEW_USER;
            BigDecimal rewardInviter = INVITER;
            //ColaUserKyc userKycInfo = dataServiceFeign.getUserKycInfo(userId);
            //if (userKycInfo.getTimestamp()>1545710400000L){ // 超过这个时间点不发奖励
            //    return true;
            //}
            //if (userKycInfo.getTimestamp()<1544760000000L){
            //    reward = new BigDecimal(400);
            //    rewardInviter = new BigDecimal(160);
            //}
            signUp.setId(UUID.randomUUID().toString());
            signUp.setTimestamp(System.currentTimeMillis());
            signUp.setNumber(reward);
            // 给当前用户从200,给他的邀请人冲80,记录日志
            boolean success = dataServiceFeign.transformBalance(UserConstant.SYS_ACCOUNT_ID,userId,"COLA",false,false,reward,SystemBalanceConstant.REWARD_SYSTEM,"系统发放注册奖励");

            if (success){
                mapper.insert(signUp);
                ColaUserEntity user = dataServiceFeign.info(userId);
                String inviter = user.getInviter();
                if (StringUtils.isNotBlank(inviter)){
                    ColaUserEntity userEntity = dataServiceFeign.infoByInviterCode(inviter);
                    if (userEntity!=null){
                        success = dataServiceFeign.transformBalance(UserConstant.SYS_ACCOUNT_ID,userEntity.getSysUserID(),"COLA",false,false,rewardInviter,SystemBalanceConstant.REWARD_SYSTEM,"系统发放注册邀请人奖励");
                        if (success) {
                            signUp.setId(UUID.randomUUID().toString());
                            signUp.setIsInviterReward(1);
                            signUp.setUserId(userEntity.getSysUserID());
                            signUp.setNumber(rewardInviter);
                            signUp.setAreaCode(userEntity.getAreaCode());
                            signUp.setTelephone(userEntity.getTelPhone());
                            mapper.insert(signUp);
                        } else {
                            return false;
                        }
                    }
                }

            } else {
                return false;
            }
        }
        return true;
    }

    private void alctReward(String userId){
        ColaUserKyc userKycInfo = dataServiceFeign.getUserKycInfo(userId);
        if (userKycInfo.getTimestamp() < 1557374400000L) return;
        dataServiceFeign.transformBalance(UserConstant.SYS_ACCOUNT_ID,userId,"ALCT",false,false,ALCT_REWARD,SystemBalanceConstant.REWARD_SYSTEM,"注册获ALCT糖果");
    }



}
