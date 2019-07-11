package com.bitcola.exchange.security.auth.mapper;

import com.bitcola.exchange.security.auth.entity.ClientService;
import tk.mybatis.mapper.common.Mapper;

public interface ClientServiceMapper extends Mapper<ClientService> {
    void deleteByServiceId(String id);
}