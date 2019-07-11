package com.bitcola.exchange.chat.util;

import com.bitcola.exchange.chat.comm.Constants;
import com.bitcola.exchange.chat.comm.HTTPMethod;
import com.bitcola.exchange.chat.comm.Roles;
import com.bitcola.exchange.chat.jersey.apidemo.EasemobIMUsers;
import com.bitcola.exchange.chat.jersey.utils.JerseyUtils;
import com.bitcola.exchange.chat.jersey.vo.ClientSecretCredential;
import com.bitcola.exchange.chat.jersey.vo.Credential;
import com.bitcola.exchange.chat.jersey.vo.EndPoints;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天室
 *
 * @author zkq
 * @create 2018-11-05 21:52
 **/
public class ChatRoomUtil {

    private static final String APPKEY = Constants.APPKEY;
    private static final JsonNodeFactory factory = new JsonNodeFactory(false);
    private static Credential credential = new ClientSecretCredential(Constants.APP_CLIENT_ID,
            Constants.APP_CLIENT_SECRET, Roles.USER_ROLE_APPADMIN);

    /**
     * 创建聊天室
     *
     * {
     *     "name":"testchatroom", //聊天室名称，此属性为必须的
     *     "description":"server create chatroom", //聊天室描述，此属性为必须的
     *     "maxusers":300, //聊天室成员最大数（包括聊天室创建者），值为数值类型，默认值200，最大值5000，此属性为可选的
     *     "owner":"jma1", //聊天室的管理员，此属性为必须的
     *     "members":["jma2","jma3"] //聊天室成员，此属性为可选的，但是如果加了此项，数组元素至少一个（注：聊天室创建者jma1不需要写入到members里面）
     * }
     *
     * @param dataNode
     * @return
     */
    public static ObjectNode createChatRoom(ObjectNode dataNode){
        ObjectNode objectNode = factory.objectNode();
        try {
            JerseyWebTarget webTarget = EndPoints.CHATROOM_TARGET.resolveTemplate("org_name",
                    APPKEY.split("#")[0]).resolveTemplate("app_name",
                    APPKEY.split("#")[1]);

            objectNode = JerseyUtils.sendRequest(webTarget, dataNode, credential, HTTPMethod.METHOD_POST, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return objectNode;
    }

    /**
     * 修改聊天室
     *
     * {
     *     "name":"test chatroom", //聊天室名称，修改时值不能包含斜杠("/")。
     *     "description":"update chatroom info", //聊天室描述，修改时值不能包含斜杠("/")。
     *     "maxusers":200, //聊天室成员最大数（包括聊天室创建者），值为数值类型
     * }
     *
     * @param dataNode
     * @return
     */
    public static ObjectNode editChatRoom(ObjectNode dataNode,String chatRoomId){
        ObjectNode objectNode = factory.objectNode();
        try {
            JerseyWebTarget webTarget = EndPoints.CHATROOM_TARGET.resolveTemplate("org_name",
                    APPKEY.split("#")[0]).resolveTemplate("app_name",
                    APPKEY.split("#")[1]).path(chatRoomId);

            objectNode = JerseyUtils.sendRequest(webTarget, dataNode, credential, HTTPMethod.METHOD_PUT, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return objectNode;
    }

    /**
     * 删除聊天室
     * @param chatRoomId
     * @return
     */
    public static ObjectNode deleteChatRoom(String chatRoomId){
        ObjectNode objectNode = factory.objectNode();
        try {
            JerseyWebTarget webTarget = EndPoints.CHATROOM_TARGET.resolveTemplate("org_name",
                    APPKEY.split("#")[0]).resolveTemplate("app_name",
                    APPKEY.split("#")[1]).path(chatRoomId);

            objectNode = JerseyUtils.sendRequest(webTarget, null, credential, HTTPMethod.METHOD_DELETE, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return objectNode;
    }


    /**
     * 添加成员
     * @param chatRoomId
     * @param userId
     * @return
     */
    public static ObjectNode addUser(String chatRoomId,String userId){
        ObjectNode objectNode = factory.objectNode();
        try {
            JerseyWebTarget webTarget = EndPoints.CHATROOM_TARGET.resolveTemplate("org_name",
                    APPKEY.split("#")[0]).resolveTemplate("app_name",
                    APPKEY.split("#")[1]).path(chatRoomId).path("users").path(userId);

            objectNode = JerseyUtils.sendRequest(webTarget, null, credential, HTTPMethod.METHOD_POST, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return objectNode;
    }
    /**
     * 添加成员
     * @param chatRoomId
     * @param userId
     * @return
     */
    public static ObjectNode removeUser(String chatRoomId,String userId){
        ObjectNode objectNode = factory.objectNode();
        try {
            JerseyWebTarget webTarget = EndPoints.CHATROOM_TARGET.resolveTemplate("org_name",
                    APPKEY.split("#")[0]).resolveTemplate("app_name",
                    APPKEY.split("#")[1]).path(chatRoomId).path("users").path(userId);

            objectNode = JerseyUtils.sendRequest(webTarget, null, credential, HTTPMethod.METHOD_DELETE, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return objectNode;
    }
    /**
     * 管理员列表
     * @param chatRoomId
     * @return
     */
    public static List<String> adminList(String chatRoomId){
        ObjectNode objectNode = factory.objectNode();
        List<String> ids = new ArrayList<>();
        try {
            JerseyWebTarget webTarget = EndPoints.CHATROOM_TARGET.resolveTemplate("org_name",
                    APPKEY.split("#")[0]).resolveTemplate("app_name",
                    APPKEY.split("#")[1]).path(chatRoomId).path("admin");

            objectNode = JerseyUtils.sendRequest(webTarget, null, credential, HTTPMethod.METHOD_GET, null);
            JsonNode data = objectNode.get("data");
            for (JsonNode datum : data) {
                String s = datum.asText();
                ids.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
    }

    /**
     * 添加管理员
     *
     * {
     *      "newadmin":"z1"
     * }
     *
     * @param chatRoomId
     * @return
     */
    public static ObjectNode addAdmin(ObjectNode dataNode,String chatRoomId){
        ObjectNode objectNode = factory.objectNode();
        try {
            JerseyWebTarget webTarget = EndPoints.CHATROOM_TARGET.resolveTemplate("org_name",
                    APPKEY.split("#")[0]).resolveTemplate("app_name",
                    APPKEY.split("#")[1]).path(chatRoomId).path("admin");

            objectNode = JerseyUtils.sendRequest(webTarget, dataNode, credential, HTTPMethod.METHOD_POST, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return objectNode;
    }

    /**
     * 移除 admin
     * @param chatRoomId
     * @param userId
     * @return
     */
    public static ObjectNode removeAdmin(String chatRoomId,String userId){
        ObjectNode objectNode = factory.objectNode();
        try {
            JerseyWebTarget webTarget = EndPoints.CHATROOM_TARGET.resolveTemplate("org_name",
                    APPKEY.split("#")[0]).resolveTemplate("app_name",
                    APPKEY.split("#")[1]).path(chatRoomId).path("admin").path(userId);

            objectNode = JerseyUtils.sendRequest(webTarget, null, credential, HTTPMethod.METHOD_DELETE, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return objectNode;
    }

    /**
     * 禁言列表
     * @param chatRoomId
     * @return
     */
    public static List<Map<String,Object>> muteList(String chatRoomId){
        ObjectNode objectNode = factory.objectNode();
        List<Map<String,Object>> list = new ArrayList<>();
        try {
            JerseyWebTarget webTarget = EndPoints.CHATROOM_TARGET.resolveTemplate("org_name",
                    APPKEY.split("#")[0]).resolveTemplate("app_name",
                    APPKEY.split("#")[1]).path(chatRoomId).path("mute");

            objectNode = JerseyUtils.sendRequest(webTarget, null, credential, HTTPMethod.METHOD_GET, null);
            JsonNode data = objectNode.get("data");
            for (JsonNode datum : data) {
                String user = datum.get("user").asText();
                Long expire = datum.get("expire").asLong();
                Map<String,Object> map = new HashMap<>();
                map.put("user",user);
                map.put("expire",expire);
                list.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    /**
     * 添加禁言
     *
     * {
     *      "usernames":[
     *                      "z1",
     *                      "z2",
     *                      "z3"
     *                  ],
     *      "mute_duration":86400000
     * }
     *
     *
     * @param dataNode
     * @param chatRoomId
     * @return
     */
    public static ObjectNode mute(ObjectNode dataNode,String chatRoomId){
        ObjectNode objectNode = factory.objectNode();
        try {
            JerseyWebTarget webTarget = EndPoints.CHATROOM_TARGET.resolveTemplate("org_name",
                    APPKEY.split("#")[0]).resolveTemplate("app_name",
                    APPKEY.split("#")[1]).path(chatRoomId).path("mute");

            objectNode = JerseyUtils.sendRequest(webTarget, dataNode, credential, HTTPMethod.METHOD_POST, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return objectNode;
    }

    /**
     * 移除禁言
     *
     * @param chatRoomId
     * @param users  逗号分隔,请不要加空格,可以只传一个用户 id
     * @return
     */
    public static ObjectNode removeMute(String chatRoomId,String users){
        ObjectNode objectNode = factory.objectNode();
        try {
            JerseyWebTarget webTarget = EndPoints.CHATROOM_TARGET.resolveTemplate("org_name",
                    APPKEY.split("#")[0]).resolveTemplate("app_name",
                    APPKEY.split("#")[1]).path(chatRoomId).path("mute");

            objectNode = JerseyUtils.sendRequest(webTarget, null, credential, HTTPMethod.METHOD_DELETE, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return objectNode;
    }



}
