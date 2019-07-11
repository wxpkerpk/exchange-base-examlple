package com.bitcola.exchange.security.community.feign;

import com.bitcola.community.entity.NotificationsEntity;
import com.bitcola.community.entity.NotificationsVo;
import com.bitcola.me.entity.ColaMeBalance;
import com.bitcola.me.entity.ColaUserEntity;
import com.bitcola.me.entity.ColaUserLimit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zkq
 * @create 2018-10-31 17:46
 **/
@Repository
@FeignClient(value = "dataservice")
public interface IDataServiceFeign {

    @RequestMapping(value = "donate/donate",method = RequestMethod.GET)
    public boolean donate(@RequestParam("fromUser") String fromUser, @RequestParam("toUser")String toUser,@RequestParam("number") BigDecimal number);

    @RequestMapping(value = "donate/donateNews",method = RequestMethod.GET)
    public boolean donateNews(@RequestParam("fromUser") String fromUser, @RequestParam("toUser")String toUser,@RequestParam("number") BigDecimal number,@RequestParam("coinCode") String coinCode);


    @RequestMapping(value = "community/add",method = RequestMethod.POST)
    public Integer insert(@RequestBody NotificationsEntity entity);

    @RequestMapping(value = "community/list",method = RequestMethod.GET)
    public List<NotificationsVo> list(@RequestParam("size") Integer size, @RequestParam("timestamp")Long timestamp, @RequestParam("userId")String userId);

    @RequestMapping(value = "community/notReadNumber",method = RequestMethod.GET)
    public Long notReadNumber(@RequestParam("userId")String userId);

    @RequestMapping(value = "community/read",method = RequestMethod.GET)
    public Integer read(@RequestParam("id")String id);

    @RequestMapping(value = "community/readAll",method = RequestMethod.GET)
    public Integer readAll(@RequestParam("userId")String userId);

    @RequestMapping(value = "keyWordLimit/contain",method = RequestMethod.GET)
    public boolean contain(@RequestParam("str")String str);

    @RequestMapping(value = "keyWordLimit/replace",method = RequestMethod.GET)
    public String replace(@RequestParam("str")String str);

    @RequestMapping(value = "userLimit/getUserLimit",method = RequestMethod.GET)
    public ColaUserLimit getUserLimit(@RequestParam("userId")String userId, @RequestParam("module")String module);

    @RequestMapping(value = "user/info",method = RequestMethod.GET)
    public ColaUserEntity info(@RequestParam("userId") String userId);

    @RequestMapping(value = "user/getColaToken",method = RequestMethod.GET)
    public ColaMeBalance getColaToken(@RequestParam("userId") String userId);

    //public int donate(@RequestParam("userId") String userId, BigDecimal amount);

    @RequestMapping(value = "user/infoByIds",method = RequestMethod.POST)
    public List<ColaUserEntity> infoByIds(@RequestBody ArrayList<String> userIds);

    @RequestMapping(value = "user/verifyPin",method = RequestMethod.GET)
    boolean verifyPin(@RequestParam("userId")String userID, @RequestParam("pin")String pin);


}
