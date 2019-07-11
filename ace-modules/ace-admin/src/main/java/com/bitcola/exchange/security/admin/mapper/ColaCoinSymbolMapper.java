package com.bitcola.exchange.security.admin.mapper;

import com.bitcola.me.entity.ColaCoinSymbol;
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

}
