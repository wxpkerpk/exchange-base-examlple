package com.bitcola.chain.mapper;

import com.bitcola.chain.entity.ColaChainModule;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface ColaChainModuleMapper extends Mapper<ColaChainModule> {
    ColaChainModule getNotRunningModule();

}
