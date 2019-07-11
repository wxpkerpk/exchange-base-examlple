package com.bitcola.exchange.launchpad.mapper;

import com.bitcola.exchange.launchpad.entity.ColaResonanceDestroy;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;

@Repository
public interface ColaResonanceDestroyMapper extends Mapper<ColaResonanceDestroy> {

    BigDecimal countNumber(@Param("coinCode") String coinCode);

}
