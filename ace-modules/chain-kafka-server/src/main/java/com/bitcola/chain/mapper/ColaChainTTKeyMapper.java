package com.bitcola.chain.mapper;


import com.bitcola.chain.entity.ColaChainEthKey;
import com.bitcola.chain.entity.ColaChainTTKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

@Repository
public interface ColaChainTTKeyMapper extends Mapper<ColaChainTTKey> {

    Long getStartBlockNumber();

    void addBlockNumber(@Param("currentScanNumber") long currentScanNumber);

    List<Map<String, Object>> getTransferInfo(@Param("start") long start, @Param("end")long end);
}
