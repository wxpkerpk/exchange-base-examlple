package com.bitcola.exchange.mapper;

import com.bitcola.exchange.entity.Balance;
import com.bitcola.exchange.entity.BatchBalance;
import com.bitcola.exchange.script.data.Config;
import com.bitcola.exchange.script.data.PairScale;
import com.bitcola.exchange.script.vo.BalanceDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface ScriptMapper extends Mapper<Config>{


    List<PairScale> getPairScale();

    List<BalanceDetail> getBalance(@Param("userId") String userId);


}
