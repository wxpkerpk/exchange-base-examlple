package com.bitcola.exchange.security.me.mapper;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ColaDealingConsumerMapper {
    List<Map<String, String>> consumerList();
}
