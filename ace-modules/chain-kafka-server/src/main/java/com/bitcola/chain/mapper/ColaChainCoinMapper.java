package com.bitcola.chain.mapper;


import com.bitcola.chain.entity.ColaChainCoin;
import com.bitcola.exchange.security.common.msg.ColaChainDepositResponse;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ColaChainCoinMapper extends Mapper<ColaChainCoin> {
}
