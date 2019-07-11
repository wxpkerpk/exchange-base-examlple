package com.bitcola.exchange.chat.controller;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.exchange.chat.comm.Constants;
import com.bitcola.exchange.chat.jersey.apidemo.EasemobIMUsers;
import com.bitcola.exchange.chat.jersey.apidemo.EasemobMessages;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 推送
 *
 * @author zkq
 * @create 2018-11-02 21:20
 **/
@RestController
@RequestMapping("users")
public class ColaUsersController {

    /**
     * 创建用户
     * @return
     */
    @RequestMapping("create")
    public boolean create( String userId,String nickname){
        ObjectNode datanode = JsonNodeFactory.instance.objectNode();
        datanode.put("username",userId);
        datanode.put("password", Constants.DEFAULT_PASSWORD);
        datanode.put("nickname", nickname);
        ObjectNode createNewIMUserSingleNode = EasemobIMUsers.createNewIMUserSingle(datanode);
        if (null != createNewIMUserSingleNode) {
            return true;
        }
        return false;
    }

    @RequestMapping(value = "createUsers",method = RequestMethod.POST)
    public static void createUsers(@RequestBody List<Map<String, String>> list) throws Exception{
        List<Object> users = new ArrayList<>();
        for (Map<String, String> map : list) {
            Map<String,String> user = new HashMap<>();
            user.put("username",map.get("id"));
            user.put("password", Constants.DEFAULT_PASSWORD);
            user.put("nickname", map.get("nickname"));
            users.add(user);
            if (users.size() >= 20){
                EasemobIMUsers.createUsers(JSONObject.toJSONString(users));
                users.clear();
            }
        }
        if (users.size()>0){
            EasemobIMUsers.createUsers(JSONObject.toJSONString(users));
        }
    }


    /**
     * 获取全部用户 ID
     * @return
     */
    @RequestMapping("getUsers")
    public List<String> getUsers(){
        ObjectNode imUsers = EasemobIMUsers.getIMUsers(1000,null);
        JsonNode entitys = imUsers.get("entities");
        JsonNode cursor = imUsers.get("cursor");
        List<String> list = new ArrayList<>();
        for (JsonNode entity : entitys) {
            String username = entity.get("username").asText();
            list.add(username);
        }
        while (cursor != null){
            imUsers = EasemobIMUsers.getIMUsers(1000,cursor.asText());
            entitys = imUsers.get("entities");
            cursor = imUsers.get("cursor");
            System.out.println(cursor);
            for (JsonNode entity : entitys) {
                String username = entity.get("username").asText();
                list.add(username);
            }
        }
        return list;
    }

    /**
     * 更新昵称
     * @param userId
     * @param nickname
     * @return
     */
    @RequestMapping("update")
    public boolean update( String userId,String nickname){
        ObjectNode datanode = JsonNodeFactory.instance.objectNode();
        datanode.put("nickname", nickname);
        ObjectNode createNewIMUserSingleNode = EasemobIMUsers.updateUserNickname(datanode,userId);
        if (null != createNewIMUserSingleNode) {
            return true;
        }
        return false;
    }

    /**
     * 加好友
     * @param
     * @param friendId
     * @return
     */
    @RequestMapping("friend")
    public boolean friend(String userId,String friendId){
        ObjectNode addFriendSingleNode = EasemobIMUsers.addFriendSingle(userId, friendId);
        if (null != addFriendSingleNode) {
            return true;
        }
        return false;
    }

    /**
     * 解除好友关系
     * @param userId
     * @param friendId
     * @return
     */
    @RequestMapping("deleteFriendSingle")
    public boolean deleteFriendSingle(String userId,String friendId){
        ObjectNode deleteFriendSingleNode = EasemobIMUsers.deleteFriendSingle(userId, friendId);
        if (null != deleteFriendSingleNode) {
            return true;
        }
        return false;
    }


    /**
     * 检测用户是否在线
     * @return
     */
    @RequestMapping(value = "isOnline",method = RequestMethod.GET)
    public boolean isOnline(@RequestParam("userId") String userId){
        try {
            ObjectNode usernode=EasemobMessages.getUserStatus(userId);
            if (usernode.get("data").get(userId).asText().equals("online")){
                return true;
            }
            return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }


}
