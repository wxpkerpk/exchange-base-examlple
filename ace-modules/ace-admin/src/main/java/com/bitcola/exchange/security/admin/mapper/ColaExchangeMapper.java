package com.bitcola.exchange.security.admin.mapper;

import com.bitcola.exchange.security.common.util.AdminQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ColaExchangeMapper {
    List<Map<String, Object>> page(AdminQuery query);

    Long total(AdminQuery query);
}
