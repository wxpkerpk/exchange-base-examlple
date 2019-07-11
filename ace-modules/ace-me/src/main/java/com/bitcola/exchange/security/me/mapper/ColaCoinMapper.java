package com.bitcola.exchange.security.me.mapper;

import com.bitcola.me.entity.ColaCoin;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 币种表
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:17
 */
@Repository
public interface ColaCoinMapper extends Mapper<ColaCoin> {
    ColaCoin getByCoinCode(@Param("coincode") String coinCode);

    String getPricePair(@Param("coin")String coin);

    List<Map<String,String>> list();


}
