package com.bitcola.chain.mapper;

import com.bitcola.chain.entity.ColaChainEthToken;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface ColaChainEthTokenMapper extends Mapper<ColaChainEthToken> {

    ColaChainEthToken getEthTokenByContract(@Param("contract") String contract);
}
