package com.bitcola.chain.mapper;


import com.bitcola.chain.entity.ColaChainEosToken;
import com.bitcola.chain.entity.ColaChainXlmToken;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface ColaChainXlmTokenMapper extends Mapper<ColaChainXlmToken> {

    String getCoinCodeByTokenName(@Param("tokenCode") String tokenCode,@Param("tokenIssuer") String tokenIssuer);

}
