package com.bitcola.activity.mapper;

import com.bitcola.activity.entity.ColaIsoDestroy;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;

@Repository
public interface ColaIsoDestroyMapper extends Mapper<ColaIsoDestroy> {

    BigDecimal countNumber();

}
