package com.bitcola.dataservice.mapper;

import com.bitcola.caculate.entity.RewardLog;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface RewardMapper  extends Mapper<RewardLog> {
}
