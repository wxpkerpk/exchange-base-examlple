package com.bitcola.activity.feign;

import com.bitcola.me.entity.ColaUserEntity;
import com.bitcola.me.entity.ColaUserKyc;
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

    @RequestMapping(value = "user/info",method = RequestMethod.GET)
    public ColaUserEntity info(@RequestParam("userId") String userId);
    @RequestMapping(value = "user/infoByInviterCode",method = RequestMethod.GET)
    public ColaUserEntity infoByInviterCode(@RequestParam("inviterCode") String inviterCode);

    @RequestMapping(value = "systemBalance/transformBalance",method = RequestMethod.GET)
    public boolean transformBalance(@RequestParam("fromUser") String fromUser, @RequestParam("toUser") String toUser,
                                    @RequestParam("coinCode")String coinCode,@RequestParam("fromFrozen")boolean fromFrozen,
                                    @RequestParam("toFrozen")boolean toFrozen,@RequestParam("number")BigDecimal number,
                                    @RequestParam("type")String type, @RequestParam("description")String description);

    @RequestMapping(value = "user/getUserKycInfo",method = RequestMethod.GET)
    public ColaUserKyc getUserKycInfo(@RequestParam("userId")String userId);

}
