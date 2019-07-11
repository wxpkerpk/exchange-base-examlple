package com.bitcola.chain.mapper;


import com.bitcola.chain.entity.ColaChainXemToken;
import com.bitcola.chain.entity.ColaChainXlmToken;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface ColaChainXemTokenMapper extends Mapper<ColaChainXemToken> {

    String getCoinCodeByTokenName(@Param("tokenName") String tokenName, @Param("namespaceId") String namespaceId);

}
