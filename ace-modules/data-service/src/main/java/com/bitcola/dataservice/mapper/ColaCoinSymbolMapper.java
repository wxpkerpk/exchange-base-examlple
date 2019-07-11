package com.bitcola.dataservice.mapper;

import com.bitcola.me.entity.ColaCoinSymbol;
import com.bitcola.me.entity.ColaCoinUserchoose;
import com.bitcola.me.entity.ColaUserChooseVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

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

    ColaCoinSymbol getSymbol(@Param("symbol") String symbol,@Param("code") String code);



    List<ColaUserChooseVo> list(@Param("userid")String userID);

    List<String> getSymbols();
}
