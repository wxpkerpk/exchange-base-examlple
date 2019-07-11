package com.bitcola.chain.mapper;


import com.bitcola.chain.entity.ColaChainKey;
import com.bitcola.exchange.security.common.msg.ColaChainDepositResponse;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ColaChainKeyMapper extends Mapper<ColaChainKey> {

}
