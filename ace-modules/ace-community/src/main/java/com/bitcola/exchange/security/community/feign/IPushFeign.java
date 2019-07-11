package com.bitcola.exchange.security.community.feign;

import com.bitcola.exchange.security.common.msg.AppResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author zkq
 * @create 2018-11-05 16:09
 **/
@Repository
@FeignClient(value = "bitcola-push")
public interface IPushFeign {

    @RequestMapping("push/one")
    public AppResponse one(@RequestParam("message") String message,@RequestParam("userId") String userId);
}
