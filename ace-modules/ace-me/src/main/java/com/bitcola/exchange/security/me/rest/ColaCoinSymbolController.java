package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.auth.client.annotation.IgnoreUserToken;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.rest.BaseController;
import com.bitcola.exchange.security.me.biz.ColaCoinBiz;
import com.bitcola.exchange.security.me.biz.ColaCoinSymbolBiz;
import com.bitcola.me.entity.ColaCoin;
import com.bitcola.me.entity.ColaCoinSymbol;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("colaCoinSymbol")
public class ColaCoinSymbolController extends BaseController<ColaCoinSymbolBiz,ColaCoinSymbol> {



    @Autowired
    ColaCoinSymbolBiz biz;

    @Autowired
    ColaCoinBiz colaCoinBiz;

    /**
     * 获得交易介质
     * @return
     */
    @RequestMapping("getSymbol")
    public AppResponse getSymbol(){
        List<Map<String,Object>> symbol = biz.getSymbol();
        return AppResponse.ok().data(symbol);
    }

    @IgnoreUserToken
    @RequestMapping("exchangeInfo")
    public AppResponse exchangeInfo(String coinCode){
        List<String> info = biz.exchangeInfo(coinCode);
        List<Map<String,Object>> result = new ArrayList<>();
        for (Map<String,Object> map : biz.getSymbol()) {
            Map<String,Object> item = new HashMap<>();
            String name = map.get("name").toString();
            item.put("pair",coinCode+"_"+name);
            if (info.contains(coinCode+"_"+name)){
                item.put("allow",true);
            } else {
                item.put("allow",false);
            }
            result.add(item);
        }
        return AppResponse.ok().data(result);
    }



    /**
     * 获得交易对
     * @return
     */
    @RequestMapping("getCoinSymbolBySymbol")
    public AppResponse getCoinSymbolBySymbol(String symbol){
        List<ColaCoinSymbol> list = biz.getCoinSymbolBySymbol(symbol);
        return AppResponse.ok().data(list);
    }


    /**
     * 新增交易对
     * @param symbol
     * @return
     */
    @RequestMapping(value = "insertCoinSymbol",method = RequestMethod.POST)
    public AppResponse insertCoinSymbol(@RequestBody ColaCoinSymbol symbol){
        if (StringUtils.isBlank(symbol.getCoinCode()) || StringUtils.isBlank(symbol.getSymbol()) || StringUtils.isBlank(symbol.getFees().toString())){
            return AppResponse.paramsError();
        }
        if (symbol.getMin() == null){
            symbol.setMin(BigDecimal.ZERO);
        }
        if (symbol.getMax() == null){
            symbol.setMax(BigDecimal.ZERO);
        }
        //判断是否有当前交易对,防止重复添加
        int i = biz.repeat(symbol);
        if (i == 0){
            symbol.setId(UUID.randomUUID().toString());
            biz.insertCoinSymbol(symbol);
        }
        return AppResponse.ok();
    }


    /**
     * 交易对精度配置
     * @return
     */
    @Deprecated
    @RequestMapping("getPairPrecision")
    public AppResponse getPairPrecision(String pair){
        List list = new ArrayList();
        if (StringUtils.isNotBlank(pair)){
            String[] pairs = pair.split("_");
            ColaCoinSymbol query = new ColaCoinSymbol();
            query.setCoinCode(pairs[0]);
            query.setSymbol(pairs[1]);
            ColaCoinSymbol colaCoinSymbol = biz.selectOne(query);
            if (colaCoinSymbol == null){
                return AppResponse.paramsError();
            }
            list.add(getPairPrecision(colaCoinSymbol));
            return AppResponse.ok().data(list);
        }
        List<ColaCoinSymbol> colaCoinSymbols = baseBiz.selectListAll();
        for (ColaCoinSymbol colaCoinSymbol : colaCoinSymbols) {
            list.add(getPairPrecision(colaCoinSymbol));
        }
        return AppResponse.ok().data(list);
    }

    private Map<String,Object> getPairPrecision(ColaCoinSymbol colaCoinSymbol){
        Map<String,Object> map = new HashMap<>();
        Map<String,Integer> m = new HashMap<>();
        m.put("priceScale",colaCoinSymbol.getPriceScale());
        m.put("amountScale",colaCoinSymbol.getAmountScale());
        map.put("pair",colaCoinSymbol.getCoinCode()+"_"+colaCoinSymbol.getSymbol());
        map.put("precision",m);
        return map;
    }

    @RequestMapping("getAllPrecision")
    public AppResponse getAllPrecision(){
        Map<String,Object> result = new HashMap<>();
        List symbol = new ArrayList();
        List<ColaCoinSymbol> colaCoinSymbols = baseBiz.selectListAll();
        for (ColaCoinSymbol colaCoinSymbol : colaCoinSymbols) {
            symbol.add(getPairPrecision(colaCoinSymbol));
        }
        List<ColaCoin> colaCoins = colaCoinBiz.selectListAll();
        List coin = new ArrayList();
        for (ColaCoin colaCoin : colaCoins) {
            Map<String,Object> map = new HashMap<>();
            Map<String,Integer> m = new LinkedHashMap<>();
            map.put("coin",colaCoin.getCoinCode());
            m.put("CNY",colaCoin.getCnyScale());
            m.put("USD",colaCoin.getUsdScale());
            m.put("EUR",colaCoin.getEurScale());
            m.put("GBP",colaCoin.getGbpScale());
            m.put("JPY",colaCoin.getJpyScale());
            m.put("scale",colaCoin.getPrec());
            map.put("precision",m);
            coin.add(map);
        }
        result.put("pair",symbol);
        result.put("coin",coin);
        return AppResponse.ok().data(result);
    }





}