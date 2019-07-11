package com.bitcola.chain.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author zkq
 * @create 2018-11-14 10:12
 **/
@FeignClient(value = "bitcola-push")
@Repository
public interface IPushFeign {

    @RequestMapping(value = "notice/warning",method = RequestMethod.GET)
    void smsWarning(@RequestParam("message") String message, @RequestParam("telephone") String telephone);

}
