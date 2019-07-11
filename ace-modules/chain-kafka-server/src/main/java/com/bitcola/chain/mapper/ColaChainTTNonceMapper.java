package com.bitcola.chain.mapper;


import com.bitcola.chain.entity.ColaChainNonce;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface ColaChainTTNonceMapper extends Mapper<ColaChainNonce> {
}
