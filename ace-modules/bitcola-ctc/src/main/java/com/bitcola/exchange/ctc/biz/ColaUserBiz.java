package com.bitcola.exchange.ctc.biz;

import com.bitcola.exchange.ctc.entity.ColaUser;
import com.bitcola.exchange.ctc.mapper.ColaUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zkq
 * @create 2019-05-07 17:48
 **/
@Service
public class ColaUserBiz {

    @Autowired
    ColaUserMapper userMapper;

    /**
     * 查询用户各种信息
     */
    public ColaUser getUserInfo(String userId){
        return userMapper.getUserInfo(userId);
    }


}
