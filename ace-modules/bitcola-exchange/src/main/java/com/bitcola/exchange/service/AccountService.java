package com.bitcola.exchange.service;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.exchange.entity.Balance;
import com.bitcola.exchange.feign.IConfigFeign;
import com.bitcola.exchange.mapper.BalanceMapper;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author zkq
 * @create 2019-02-13 11:53
 **/
@Service
public class AccountService {

    @Autowired
    IConfigFeign configFeign;


    @Cached(name = "inviterRewardRate",cacheType = CacheType.LOCAL, expire = 60)
    public BigDecimal getTransactionInviterRate(){
        return new BigDecimal(configFeign.getConfig("referral_rewards_transaction_fees"));
    }

}
