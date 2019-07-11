package com.bitcola.exchange.security.admin.feign;

import com.bitcola.me.entity.ColaUserEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "bitcola-activity")
@Repository
public interface IActivityFeign {

    @RequestMapping(value = "cola/signUp",method = RequestMethod.GET)
    public boolean kycReward(@RequestParam("userId") String userId);

    @RequestMapping(value = "cola/initInnerTestUserBalance",method = RequestMethod.GET)
    public boolean initInnerTestUserBalance(@RequestParam("userId") String userId);

}
