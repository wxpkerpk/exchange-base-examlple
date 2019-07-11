package com.bitcola.exchange.security.me.biz;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.me.feign.IPushFeign;
import com.bitcola.exchange.security.me.feign.IExchangeFeign;
import com.bitcola.exchange.security.me.mapper.ColaMeBalanceMapper;
import com.bitcola.me.entity.ColaMeBalance;
import com.bitcola.me.entity.ColaUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bitcola.me.entity.ColaCoin;
import com.bitcola.exchange.security.me.mapper.ColaCoinMapper;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 币种表
 *
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:17
 */
@Service
@Transactional
public class ColaCoinBiz extends BaseBiz<ColaCoinMapper,ColaCoin> {

    @Autowired
    ColaMeBalanceBiz balanceBiz;

    @Autowired
    ColaUserBiz userBiz;

    @Autowired
    IExchangeFeign exchangeFeign;

    @Autowired
    IPushFeign pushFeign;

    @Autowired
    ColaMeBalanceMapper balanceMapper;

    /**
     * 通过 coinCode获取币种信息
     * @param coinCode
     * @return
     */
    public ColaCoin getByCoinCode(String coinCode) {

        return mapper.getByCoinCode(coinCode);
    }


    /**
     * 生成或者补齐当前登录用户币种
     *
     * @author zkq
     * @date 2018/8/1 14:15
     * @param
     * @return void
     */
    public void initUserBalance(String userID) {
        balanceBiz.initUserBalance(userID);
    }
    /**
     * 生成或者补齐当前登录用户币种
     *
     * @author zkq
     * @date 2018/8/1 14:15
     * @param
     * @return void
     */
    public void initUserBalance() {
        this.initUserBalance(null);
    }


    /**
     * 添加币种,并且补齐用户钱包
     * @param coin
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void addCoin(ColaCoin coin) {
        mapper.insert(coin);
        List<ColaUser> colaUsers = userBiz.selectListAll();
        for (ColaUser colaUser : colaUsers) {
            String userId = colaUser.getSysUserId();
            ColaMeBalance entity = new ColaMeBalance();
            entity.setId(userId+coin.getCoinCode());
            entity.setCoinCode(coin.getCoinCode());
            entity.setUserId(userId);
            entity.setBalanceFrozen(BigDecimal.ZERO);
            entity.setBalanceAvailable(BigDecimal.ZERO);
            balanceMapper.insertWithSign(entity, EncoderUtil.BALANCE_KEY);
        }
    }

    /**
     * 币对 usdt 的价格
     * @param coin
     * @return
     */
    public BigDecimal getCoinWorth(String coin) {
        if (coin.equals("EOS")){
            return pushFeign.eos();
        }
        if (coin.equals("ETH")){
            return pushFeign.eth();
        }
        if (coin.equals("BTC")){
            return pushFeign.btc();
        }
        if (coin.equals("USDT")){
            return BigDecimal.ONE;
        }
        AppResponse usdtAssessment = exchangeFeign.getUsdPrice(coin);
        BigDecimal m = new BigDecimal(usdtAssessment.getData().toString());
        return m;
    }

    @Cached(key = "getPricePair + #coin", cacheType = CacheType.LOCAL, expire = 3600)
    public String getPricePair(String coin){
        return mapper.getPricePair(coin);
    }

    @Cached(key = "coinCodelist", cacheType = CacheType.LOCAL, expire = 3600)
    public List<Map<String,String>> list() {
        return mapper.list();
    }

    public Map<String, BigDecimal> allCoinWorth() {
        Map<String, BigDecimal> map = new HashMap<>();
        List<ColaCoin> colaCoins = mapper.selectAll();
        for (ColaCoin colaCoin : colaCoins) {
            String coinCode = colaCoin.getCoinCode();
            map.put(coinCode,getCoinWorth(coinCode));
        }
        return map;
    }
}