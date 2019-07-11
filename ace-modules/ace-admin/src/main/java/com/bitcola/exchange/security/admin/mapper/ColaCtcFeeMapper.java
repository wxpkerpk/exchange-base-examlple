package com.bitcola.exchange.security.admin.mapper;

import com.bitcola.ctc.ColaCtcFee;
import com.bitcola.ctc.ColaCtcOrder;
import com.bitcola.exchange.security.common.util.AdminQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ColaCtcFeeMapper extends Mapper<ColaCtcFee> {
}
