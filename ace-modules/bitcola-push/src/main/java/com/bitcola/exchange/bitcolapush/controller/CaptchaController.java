package com.bitcola.exchange.bitcolapush.controller;

import com.bitcola.exchange.bitcolapush.http.Email;
import com.bitcola.exchange.bitcolapush.http.SMSUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zkq
 * @create 2018-11-13 18:43
 **/
@RestController
@RequestMapping("captcha")
public class CaptchaController {

    @RequestMapping(value = "sms",method = RequestMethod.GET)
    public void sms(String captcha,String areaCode,String telephone){
        SMSUtils.sendCaptchaSMS(areaCode,telephone,captcha);
    }


    @RequestMapping(value = "email",method = RequestMethod.GET)
    public void email(String language,String to,String code,String antiPhishingCode){
        Email.sendEmail(to,code,language,antiPhishingCode,"BITCOLA verification code","BitCola 验证码");
    }


}
