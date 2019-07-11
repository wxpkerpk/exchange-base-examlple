package com.bitcola.dataservice.biz;

import com.bitcola.dataservice.mapper.ColaCoinSymbolMapper;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.me.entity.ColaCoinUserchoose;
import com.bitcola.me.entity.ColaUserChooseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bitcola.me.entity.ColaCoinSymbol;

import java.util.ArrayList;
import java.util.List;

/*
 * @author:wx
 * @description:
 * @create:2018-08-28  20:07
 */
@Service
public class ColaSymbolBiz {
    @Autowired
    ColaCoinSymbolMapper colaCoinSymbolMapper;


    public   List<String>getAllSymbol()
    {
        List<String>symbolCodes=new ArrayList<>();

        List<ColaCoinSymbol>colaCoinSymbols= colaCoinSymbolMapper.selectAll();
        for(Object colaCoinSymbol:colaCoinSymbols){
             Class clz=   colaCoinSymbol.getClass();
             Class clz2=ColaCoinSymbol.class;

             boolean isSame=colaCoinSymbol instanceof ColaCoinSymbol;

            if(colaCoinSymbol instanceof ColaCoinSymbol) {



                ColaCoinSymbol c=(ColaCoinSymbol)colaCoinSymbol;
                String code = c.getCoinCode() + "_" + c.getSymbol();
                symbolCodes.add(code);
            }
        }
        return symbolCodes;
    }


    public List<ColaCoinSymbol>selectBySymbol(String symbol)
    {
        return colaCoinSymbolMapper.getCoinSymbolBySymbol(symbol);
    }
    public ColaCoinSymbol getPair(String symbol,String code){

      return   colaCoinSymbolMapper.getSymbol(symbol,code);
    }


    public List<ColaUserChooseVo> list(String userId) {
        return colaCoinSymbolMapper.list(userId);
    }

    public List<String> getSymbols() {
        return colaCoinSymbolMapper.getSymbols();
    }
}
