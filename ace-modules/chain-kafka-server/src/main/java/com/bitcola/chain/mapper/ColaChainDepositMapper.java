package com.bitcola.chain.mapper;


import com.bitcola.exchange.security.common.msg.ColaChainDepositResponse;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Set;

@Repository
public interface ColaChainDepositMapper extends Mapper<ColaChainDepositResponse> {

    List<ColaChainDepositResponse> unRecord(@Param("module")String module);

    List<ColaChainDepositResponse> unConfirm(@Param("module") String module);

    List<ColaChainDepositResponse> getDepositOrder(@Param("module")String module, @Param("start")long start, @Param("end")long end);
}
