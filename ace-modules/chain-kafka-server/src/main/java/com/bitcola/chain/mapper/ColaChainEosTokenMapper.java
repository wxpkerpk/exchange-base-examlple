package com.bitcola.chain.mapper;


import com.bitcola.chain.entity.ColaChainEosToken;
import com.bitcola.exchange.security.common.msg.ColaChainDepositResponse;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ColaChainEosTokenMapper extends Mapper<ColaChainEosToken> {

    String getCoinCodeByTokenName(@Param("tokenName") String tokenName);

}
