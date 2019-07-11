package com.bitcola.exchange.mapper;

import com.bitcola.caculate.entity.RewardLog;
import com.bitcola.me.entity.ColaSystemBalance;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ColaSystemBalanceMapper extends Mapper<ColaSystemBalance> {
    void batchInsert(List<ColaSystemBalance> record);
    void batchInsertReward(List<RewardLog> record);
}
