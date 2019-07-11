package com.bitcola.exchange.launchpad.mapper;

import com.bitcola.exchange.launchpad.entity.ColaResonanceInviterRewardLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface ColaResonanceInviterRewardLogMapper extends Mapper<ColaResonanceInviterRewardLog> {


    List<ColaResonanceInviterRewardLog> inviterRewardLog(@Param("userId") String userId,@Param("coinCode") String coinCode);
}
