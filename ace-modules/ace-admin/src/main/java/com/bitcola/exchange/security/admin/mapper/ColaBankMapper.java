package com.bitcola.exchange.security.admin.mapper;

import com.bitcola.exchange.security.admin.entity.ColaBank;
import com.bitcola.exchange.security.admin.entity.ColaSms;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface ColaBankMapper extends Mapper<ColaBank> {
}
