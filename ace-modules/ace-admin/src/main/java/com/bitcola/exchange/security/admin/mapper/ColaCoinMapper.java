package com.bitcola.exchange.security.admin.mapper;


import com.bitcola.exchange.security.common.util.AdminQuery;
import com.bitcola.me.entity.ColaCoin;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;
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


    List<Map<String, Object>> coinApply(AdminQuery query);

    Long countCoinApply(AdminQuery query);

    void insertCoinEosToken(@Param("coinCode") String coinCode, @Param("tokenName")String tokenName,
                            @Param("symbol") String symbol,@Param("precision") int precision);

    void insertCoinEthToken(@Param("coinCode")String coinCode, @Param("contract")String contract,
                            @Param("minAutoTransferToHot") BigDecimal minAutoTransferToHot);

    List<Map<String, Object>> eosTokenList();

    List<Map<String, Object>> ethTokenList();

    void insertCoinXlmToken(@Param("coinCode")String coinCode, @Param("tokenCode")String tokenCode, @Param("tokenIssuer")String tokenIssuer);

    List<Map<String, Object>> xlmTokenList();
}
