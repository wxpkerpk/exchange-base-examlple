package com.bitcola.exchange.chat.controller;

import com.bitcola.exchange.chat.jersey.apidemo.EasemobMessages;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author zkq
 * @create 2019-01-29 12:28
 **/
@RestController
@RequestMapping("message")
public class ColaChatMessageController {

    @RequestMapping("sendMessage")
    public void sendMessage(String message, String toUser ,String fromUser){
        String from = fromUser;
        String targetTypeus = "users";
        ObjectNode ext = EasemobMessages.factory.objectNode();
        ArrayNode targetusers = EasemobMessages.factory.arrayNode();
        targetusers.add(toUser);
        ObjectNode txtmsg = EasemobMessages.factory.objectNode();
        txtmsg.put("msg", message);
        txtmsg.put("type","txt");
        EasemobMessages.sendMessages(targetTypeus, targetusers, txtmsg, from, ext);
    }
}
