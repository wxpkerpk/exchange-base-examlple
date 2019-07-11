package com.bitcola.exchange.security.community.feign;

import com.bitcola.exchange.security.common.msg.AppResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Repository
@FeignClient(value = "bitcola-exchange")
public interface IExchangeFeign {

    @RequestMapping(value = "getBalance")
    AppResponse getBalance(@RequestParam("coinCode")String coinCode);

    @RequestMapping(value = "getCoinPrice")
    AppResponse<BigDecimal> getCoinPrice(@RequestParam("coinCode")String coinCode);
}
