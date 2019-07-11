package com.bitcola.exchange.security.me.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-11-06 12:03
 **/
@FeignClient(value = "bitcola-chat")
@Repository
public interface IChatFeign {

    @RequestMapping(value = "users/create",method = RequestMethod.GET)
    public boolean create(@RequestParam("userId") String userId, @RequestParam("nickname") String nickname);

    @RequestMapping(value = "users/update",method = RequestMethod.GET)
    public boolean update(@RequestParam("userId") String userId, @RequestParam("nickname") String nickname);

    @RequestMapping(value = "users/getUsers",method = RequestMethod.GET)
    public List<String> getUsers();

    @RequestMapping(value = "users/createUsers",method = RequestMethod.POST)
    void createUsers(@RequestBody List<Map<String, String>> list);

    @RequestMapping(value = "message/sendMessage",method = RequestMethod.GET)
    void sendChatMessage(@RequestParam("toUser") String toUser,@RequestParam("fromUser") String fromUser, @RequestParam("message") String message);

    @RequestMapping(value = "users/isOnline",method = RequestMethod.GET)
    public boolean isOnline(@RequestParam("userId") String userId);

//    @RequestMapping(value = "message/sendMessageToConsumer",method = RequestMethod.GET)
//    void sendChatMessageToConsumer(@RequestParam("toUser") String toUser,@RequestParam("fromUser") String fromUser, @RequestParam("message") String message);
}
