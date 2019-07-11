package com.bitcola.exchange.security.admin.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-11-08 11:53
 **/
@FeignClient(value = "dataservice")
@Service
public interface IDataServiceFeign {

    @RequestMapping(value = "systemBalance/in",method = RequestMethod.GET)
    public boolean systemBalanceIn(@RequestParam("userId") String userId, @RequestParam("amount")BigDecimal amount,
                                   @RequestParam("coinCode")String coinCode,
                                   @RequestParam("type")String type, @RequestParam("description")String description);

    @RequestMapping(value = "systemBalance/out",method = RequestMethod.GET)
    public boolean systemBalanceOut(@RequestParam("userId") String userId, @RequestParam("amount")BigDecimal amount,
                                   @RequestParam("coinCode")String coinCode,
                                   @RequestParam("type")String type, @RequestParam("description")String description);

    @RequestMapping(value = "systemBalance/transformBalance",method = RequestMethod.GET)
    public boolean transformBalance(@RequestParam("fromUser") String fromUser, @RequestParam("toUser") String toUser,
                                    @RequestParam("coinCode")String coinCode,@RequestParam("fromFrozen")boolean fromFrozen,
                                    @RequestParam("toFrozen")boolean toFrozen,@RequestParam("number")BigDecimal number,
                                    @RequestParam("type")String type, @RequestParam("description")String description);

}
