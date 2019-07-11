package com.bitcola.exchange.security.me.mapper;

import com.bitcola.me.entity.ColaCoinSymbol;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 交易对
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:17
 */
@Repository
public interface ColaCoinSymbolMapper extends Mapper<ColaCoinSymbol> {

    /**
     * 获得当前交易对
     * @param symbol
     * @return
     */
    List<ColaCoinSymbol> getCoinSymbolBySymbol(@Param("symbol") String symbol);

    /**
     * 重复
     * @param symbol
     * @return
     */
    int repeat(ColaCoinSymbol symbol);

    List<String> exchangeInfo(@Param("coinCode")String coinCode);

    List<Map<String, Object>> getSymbol();
}
