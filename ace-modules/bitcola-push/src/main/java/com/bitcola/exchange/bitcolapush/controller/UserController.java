package com.bitcola.exchange.bitcolapush.controller;

import com.bitcola.exchange.bitcolapush.oss.OssUtil;
import com.bitcola.exchange.bitcolapush.util.UserAvatarUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

/**
 * @author zkq
 * @create 2018-12-29 22:32
 **/
@RestController
@RequestMapping("user")
public class UserController {

    @RequestMapping("getAvatar")
    public String getAvatar(String nickName) throws Exception{
        InputStream inputStream = UserAvatarUtil.generateImg(nickName);
        return OssUtil.uploadSuffix(inputStream,".png");
    }
}
