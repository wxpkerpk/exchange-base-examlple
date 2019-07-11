package com.bitcola.exchange.security.me.biz;

import com.bitcola.exchange.security.me.feign.IExchangeFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bitcola.me.entity.ColaCoinSymbol;
import com.bitcola.exchange.security.me.mapper.ColaCoinSymbolMapper;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 交易对
 *
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:17
 */
@Service
@Transactional
public class ColaCoinSymbolBiz extends BaseBiz<ColaCoinSymbolMapper,ColaCoinSymbol> {

    @Autowired
    ColaCoinSymbolMapper mapper;

    @Autowired
    IExchangeFeign exchangeFeign;

    /**
     * 获得当前交易对
     * @param symbol
     * @return
     */
    public List<ColaCoinSymbol> getCoinSymbolBySymbol(String symbol) {
        return mapper.getCoinSymbolBySymbol(symbol);
    }


    /**
     * 是否重复
     * @param symbol
     * @return
     */
    public int repeat(ColaCoinSymbol symbol) {
        return mapper.repeat(symbol);
    }


    public List<String> exchangeInfo(String coinCode) {
        return mapper.exchangeInfo(coinCode);
    }

    public List<Map<String, Object>> getSymbol() {
        return mapper.getSymbol();
    }

    public void insertCoinSymbol(ColaCoinSymbol symbol) {
        mapper.insertSelective(symbol);
        exchangeFeign.addPair(symbol.getCoinCode()+"_"+symbol.getSymbol());
    }
}