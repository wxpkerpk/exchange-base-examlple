package com.bitcola.chain.mapper;


import com.bitcola.chain.entity.ColaChainEthKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

@Repository
public interface ColaChainEthKeyMapper extends Mapper<ColaChainEthKey> {

    String getToken(@Param("coinCode") String coinCode);

    Long getStartBlockNumber();

    String getCoinCode(@Param("contract")String contract);

    void addBlockNumber(@Param("blockNumber")Long blockNumber);

    List<Map<String, String>> getTransferInfo(@Param("start") long start, @Param("end")long end);
}
