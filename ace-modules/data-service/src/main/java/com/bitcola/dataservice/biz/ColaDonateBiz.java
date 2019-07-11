package com.bitcola.dataservice.biz;

import com.bitcola.community.entity.DonateEntity;
import com.bitcola.dataservice.mapper.ColaDonateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @author zkq
 * @create 2018-10-31 16:23
 **/
@Service
public class ColaDonateBiz {

    @Autowired
    ColaDonateMapper mapper;

    @Autowired
    ColaSystemBalanceBiz balanceBiz;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void donate(String fromUser, String toUser, BigDecimal number) {
      donateNews(fromUser,toUser,number,"COLA");
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void donateNews(String fromUser, String toUser, BigDecimal number,String coinCode) {
        boolean b = balanceBiz.transformBalance(fromUser, toUser, coinCode, false, false, number, null, "打赏");
        if (!b){
            throw new RuntimeException("打赏出错,余额不足");
        }
        //记录日志
        DonateEntity entity = new DonateEntity();
        entity.setCoinCode(coinCode);
        entity.setNumber(number);
        entity.setTime(System.currentTimeMillis());
        entity.setId(UUID.randomUUID().toString());
        entity.setUserId(fromUser);
        entity.setType("Donate out");
        mapper.insert(entity);
        entity = new DonateEntity();
        entity.setCoinCode(coinCode);
        entity.setNumber(number);
        entity.setTime(System.currentTimeMillis());
        entity.setId(UUID.randomUUID().toString());
        entity.setUserId(toUser);
        entity.setType("Donate in");
        mapper.insert(entity);
    }


}
