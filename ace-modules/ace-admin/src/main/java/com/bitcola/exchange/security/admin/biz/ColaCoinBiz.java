package com.bitcola.exchange.security.admin.biz;

import com.bitcola.exchange.security.admin.mapper.ColaCoinMapper;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.AdminQuery;
import com.bitcola.me.entity.ColaCoin;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-12-18 10:38
 **/
@Service
public class ColaCoinBiz extends BaseBiz<ColaCoinMapper, ColaCoin> {
    public TableResultResponse coinApply(AdminQuery query) {
        List<Map<String,Object>> list = mapper.coinApply(query);
        Long total = mapper.countCoinApply(query);
        return new TableResultResponse(total,list);
    }

    public void insertCoinEosToken(String coinCode, String tokenName, String symbol, int precision) {
        mapper.insertCoinEosToken(coinCode,tokenName,symbol,precision);
    }

    public void insertCoinEthToken(String coinCode, String contract, BigDecimal minAutoTransferToHot) {
        mapper.insertCoinEthToken(coinCode,contract,minAutoTransferToHot);
    }

    public List<Map<String, Object>> eosTokenList() {
        return mapper.eosTokenList();
    }

    public List<Map<String, Object>> ethTokenList() {
        return mapper.ethTokenList();
    }

    public void insertCoinXlmToken(String coinCode, String tokenCode, String tokenIssuer) {
        mapper.insertCoinXlmToken(coinCode,tokenCode,tokenIssuer);
    }

    public List<Map<String, Object>> xlmTokenList() {
        return mapper.xlmTokenList();
    }
}
