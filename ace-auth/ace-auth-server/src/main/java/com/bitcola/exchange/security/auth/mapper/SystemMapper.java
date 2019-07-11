package com.bitcola.exchange.security.auth.mapper;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SystemMapper {
    List<Map<String, Object>> load();
}
