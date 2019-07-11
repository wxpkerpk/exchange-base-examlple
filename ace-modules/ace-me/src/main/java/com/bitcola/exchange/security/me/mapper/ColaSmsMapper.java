package com.bitcola.exchange.security.me.mapper;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ColaSmsMapper {
    List<String> getAreaCodeList();

    List<Map<String,String>> getCountryList();

    List<Map<String, String>> countryAndAreaCode();
}
