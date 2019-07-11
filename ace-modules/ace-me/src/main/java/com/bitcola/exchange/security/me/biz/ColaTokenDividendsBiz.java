package com.bitcola.exchange.security.me.biz;

import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.TimeUtils;
import com.bitcola.exchange.security.me.feign.IConfigFeign;
import com.bitcola.exchange.security.me.mapper.ColaTokenDividendsMapper;
import com.bitcola.exchange.security.me.vo.ColaTokenDividendsVo;
import com.bitcola.me.entity.ColaMeBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-10-26 16:55
 **/
@Service
public class ColaTokenDividendsBiz {

    @Autowired
    ColaTokenDividendsMapper mapper;

    @Autowired
    ColaMeBalanceBiz balanceBiz;

    @Autowired
    ColaCoinBiz colaCoinBiz;

    @Autowired
    IConfigFeign configFeign;

    /**
     * 返回 流通总量 , 持有量  , 分红总量(预估 USD), 每1000 COLA 分红数量(预估 USD)
     */
    public Map<String, Object> info() {
        // 这里需要获取当周报表
        String onlineTime = configFeign.getConfig("cola_exchange_week_cycle");
        long startTimestamp = TimeUtils.getWeekStartTimestamp(Long.valueOf(onlineTime));
        long endTimestamp = TimeUtils.getWeekEndTimestamp(Long.valueOf(onlineTime));
        List<Map<String,Object>> list = mapper.getWeekTransactionFees(startTimestamp,endTimestamp);
        BigDecimal totalDividends = BigDecimal.ZERO;
        for (Map<String, Object> map : list) {
            String coin = map.get("coin").toString();
            BigDecimal number = (BigDecimal)map.get("number");
            totalDividends = totalDividends.add(number.multiply(colaCoinBiz.getCoinWorth(coin)));
        }
        totalDividends = totalDividends.multiply(new BigDecimal(configFeign.getConfig("cola_token_dividends_rate")));
        Map<String, Object> result = new HashMap<>();
        ColaMeBalance colaToken = balanceBiz.getColaToken(BaseContextHandler.getUserID());
        BigDecimal tokenNumber = colaToken.getBalanceAvailable().add(colaToken.getBalanceFrozen());
        result.put("tokenNumber",tokenNumber);
        result.put("totalDividends",totalDividends);
        BigDecimal totalToken = new BigDecimal(configFeign.getConfig("cola_token_number"));// 总量 50 M
        BigDecimal remain = balanceBiz.getColaToken("8").getBalanceAvailable();// 系统留存量
        BigDecimal circulatingSupply = totalToken.subtract(remain);// 流通量
        result.put("circulatingSupply",circulatingSupply);
        if (circulatingSupply.compareTo(BigDecimal.ZERO)>0){
            BigDecimal per1000 = new BigDecimal(1000).divide(circulatingSupply,10, RoundingMode.DOWN).multiply(totalDividends);
            result.put("per1000",per1000);
        } else {
            result.put("per1000",0);
        }
        return result;
    }
    public TableResultResponse list(String keyWord, int page, int limit) {
        List<ColaTokenDividendsVo> list = mapper.list(keyWord,page,limit, BaseContextHandler.getUserID());
        Long count = mapper.count(keyWord,BaseContextHandler.getUserID());
        return new TableResultResponse(count,list);
    }


}
