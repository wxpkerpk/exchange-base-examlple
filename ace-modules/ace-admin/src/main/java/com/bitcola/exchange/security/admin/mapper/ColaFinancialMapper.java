package com.bitcola.exchange.security.admin.mapper;

import com.bitcola.exchange.security.common.util.AdminQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface ColaFinancialMapper {
    List<Map<String, Object>> page(AdminQuery query);

    Long total(AdminQuery query);

    Long countUser(AdminQuery query);

    List<Map<String, Object>> coinRange(AdminQuery query);

    List<Map<String, Object>> financialPage(AdminQuery query);

    Long financialCount(AdminQuery query);
}
