package com.bitcola.activity.mapper;

import com.bitcola.activity.entity.ColaIsoDestroy;
import com.bitcola.activity.entity.ColaIsoInviterRewardLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ColaIsoInviterRewardLogMapper extends Mapper<ColaIsoInviterRewardLog> {


    List<ColaIsoInviterRewardLog> inviterRewardLog(@Param("userId") String userId);
}
