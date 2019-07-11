package com.bitcola.exchange.chat.controller;

import com.bitcola.exchange.chat.comm.Constants;
import com.bitcola.exchange.chat.comm.HTTPMethod;
import com.bitcola.exchange.chat.comm.Roles;
import com.bitcola.exchange.chat.jersey.utils.JerseyUtils;
import com.bitcola.exchange.chat.jersey.vo.ClientSecretCredential;
import com.bitcola.exchange.chat.jersey.vo.Credential;
import com.bitcola.exchange.chat.jersey.vo.EndPoints;
import com.bitcola.exchange.chat.util.ChatRoomUtil;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 聊天室
 *      创建聊天室
 *      修改聊天室信息
 *      删除聊天室
 *      添加人员
 *      移除人员
 *      管理员列表
 *      指定管理员
 *      移除管理员
 *      禁言列表
 *      添加禁言
 *      解除禁言
 *
 *
 * @author zkq
 * @create 2018-11-05 21:43
 **/
@RestController
@RequestMapping("charRoom")
public class ColaCharRoomController {

    private static final String APPKEY = Constants.APPKEY;
    private static final JsonNodeFactory factory = new JsonNodeFactory(false);
    private static Credential credential = new ClientSecretCredential(Constants.APP_CLIENT_ID,
            Constants.APP_CLIENT_SECRET, Roles.USER_ROLE_APPADMIN);






    /**
     * 创建聊天室 (管理员可以使用)
     * @return
     */
    @RequestMapping("create")
    public static AppResponse create(){
        ObjectNode node = factory.objectNode();
        node.put("name","BitCola官方聊天室_测试");
        node.put("description","这个是测试用的聊天室");
        node.put("maxusers",5000);
        node.put("owner","100002");
        ObjectNode chatRoom = ChatRoomUtil.createChatRoom(node);
        System.out.println(chatRoom.toString());
        return AppResponse.ok();
    }

    public static void main(String[] args) throws Exception {
        create();

    }





}
