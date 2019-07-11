package com.bitcola.dataservice.biz;

import com.bitcola.dataservice.mapper.ColaUserBalanceMapper;
import com.bitcola.dataservice.mapper.ColaUserMapper;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.me.entity.ColaMeBalance;
import com.bitcola.me.entity.ColaUserEntity;
import com.bitcola.me.entity.ColaUserKyc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户信息
 *
 * @author zkq
 * @create 2018-09-03 14:13
 **/
@Service
public class ColaUserBiz {
    @Autowired
    ColaUserMapper mapper;

    @Autowired
    ColaUserBalanceMapper balanceMapper;

    public ColaUserEntity info(String userId){
        return mapper.info(userId);
    }

    public List<ColaUserEntity> infoByIds(ArrayList<String> userId) {
        return mapper.infoByIds(userId);
    }

    public boolean verifyPin(String userID, String pin) {
        ColaUserEntity info = mapper.info(userID);
        return EncoderUtil.matches(pin,info.getMoneyPassword());
    }

    public ColaMeBalance getColaToken(String userId) {
        return balanceMapper.getColaToken(userId);
    }

    public ColaUserEntity infoByInviterCode(String inviterCode) {
        return mapper.infoByInviterCode(inviterCode);
    }

    public ColaUserKyc getUserKycInfo(String userId) {
        return mapper.getUserKycInfo(userId);
    }
}
