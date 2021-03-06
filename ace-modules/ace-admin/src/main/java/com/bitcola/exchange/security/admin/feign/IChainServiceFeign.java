package com.bitcola.exchange.security.admin.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author zkq
 * @create 2018-11-14 10:12
 **/
@FeignClient(value = "chain-service")
@Repository
public interface IChainServiceFeign {

    @RequestMapping(value = "chain/trustToken",method = RequestMethod.GET)
    public String trustToken(@RequestParam("tokenCode") String tokenCode, @RequestParam("tokenIssuer") String tokenIssuer);

}
