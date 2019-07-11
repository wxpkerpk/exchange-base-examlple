package com.bitcola.activity.controller;

import com.bitcola.activity.biz.SignUpBiz;
import com.bitcola.me.entity.ColaUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author zkq
 * @create 2018-11-29 12:15
 **/
@RestController
@RequestMapping("cola")
public class SignUpController {
    @Autowired
    SignUpBiz biz;
    @RequestMapping(value = "signUp")
    public boolean reward(@RequestParam String userId){
        return biz.reward(userId);
    }




}
