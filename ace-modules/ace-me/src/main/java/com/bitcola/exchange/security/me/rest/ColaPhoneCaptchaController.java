package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.auth.client.annotation.IgnoreUserToken;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.constant.CaptchaConstant;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.me.biz.ColaSmsBiz;
import com.bitcola.exchange.security.me.constant.EmailCaptchaConstant;
import com.bitcola.exchange.security.me.feign.IPushFeign;
import com.bitcola.exchange.security.me.mapper.ColaSmsIPMapper;
import com.bitcola.exchange.security.me.util.RedisUtil;
import com.bitcola.exchange.security.me.util.SequenceFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 短信接口
 *
 * @author zkq
 * @create 2018-09-27 11:47
 **/
@RestController
@RequestMapping("sms")
public class ColaPhoneCaptchaController {

    @Autowired
    IPushFeign pushFeign;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ColaSmsBiz smsBiz;

    @Autowired
    SequenceFactory sequenceFactory;

    @Autowired
    ColaSecurityCenterController colaSecurityCenterController;

    @Autowired
    ColaSmsIPMapper smsIPMapper;

    /**
     * 短信验证码
     * @param areaCode
     * @param phoneNumber
     * @return
     */
    @IgnoreUserToken
    @RequestMapping("captcha")
    public AppResponse sendSMSCaptcha(String areaCode, String phoneNumber, String modules, HttpServletRequest request){
        if (StringUtils.isNotBlank(modules)){
            if(!CaptchaConstant.modules.contains(modules)){
                return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
            }
        }
        String ip = colaSecurityCenterController.getIp(request);
        smsIPMapper.insertLog(UUID.randomUUID().toString(),ip,phoneNumber,System.currentTimeMillis());
        long generate = sequenceFactory.generate("SMS_IP_LIMIT" + ip, 10, TimeUnit.MINUTES);
        if (generate>10){
            return AppResponse.paramsError();
        }
        Object o = redisUtil.get(EmailCaptchaConstant.SMS_CAPTCHA + areaCode + phoneNumber + modules+"_1minLimit");
        if (o!=null){
            return AppResponse.error(ResponseCode.CAPTCHA_ERROR_CODE,ResponseCode.CAPTCHA_ERROR_MESSAGE);
        }
        Integer captcha = new Random().nextInt(899999) + 100000;
        pushFeign.sms(captcha.toString(),areaCode,phoneNumber);
        redisUtil.set(EmailCaptchaConstant.SMS_CAPTCHA +areaCode+phoneNumber,captcha.toString(),10L, TimeUnit.MINUTES);
        redisUtil.set(EmailCaptchaConstant.SMS_CAPTCHA +areaCode+phoneNumber+modules+"_1minLimit",captcha.toString(),1L, TimeUnit.MINUTES);
        return AppResponse.ok();
    }

    @IgnoreUserToken
    @RequestMapping("areaCode")
    public AppResponse areaCode(){
        List<String> list = smsBiz.getAreaCodeList();
        return AppResponse.ok().data(list);
    }

    @IgnoreUserToken
    @RequestMapping("country")
    public AppResponse country(){
        List<Map<String,String>> list = smsBiz.getCountryList();
        return AppResponse.ok().data(list);
    }

    @IgnoreUserToken
    @RequestMapping("countryAndAreaCode")
    public AppResponse countryAndAreaCode(){
        List<Map<String,String>> list = smsBiz.countryAndAreaCode();
        return AppResponse.ok().data(list);
    }

    public static void main(String[] args) {
    }


}
