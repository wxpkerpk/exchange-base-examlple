package com.bitcola.chain.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ColaChainTTTokenMapper {

    String getToken(@Param("coinCode") String coinCode);

    String getCoinCode(@Param("contract")String contract);
}
