package com.bitcola.chain.mapper;


import com.bitcola.chain.entity.ColaChainCoin;
import com.bitcola.chain.entity.ColaChainNonce;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface ColaChainEthNonceMapper extends Mapper<ColaChainNonce> {
}
