package com.bitcola.exchange.security.gate.v2.feign;

import com.bitcola.exchange.security.auth.client.feign.ServiceAuthFeign;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

@Service
public interface ISystemMaintainFeign{

    @RequestMapping(value="/system/load",method = RequestMethod.GET)
    public List<Map<String,Object>> load();

}
