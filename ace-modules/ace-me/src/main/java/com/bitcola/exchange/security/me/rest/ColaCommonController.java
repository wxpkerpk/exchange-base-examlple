package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.constant.CaptchaConstant;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.me.biz.ColaSmsBiz;
import com.bitcola.exchange.security.me.biz.ColaUserBiz;
import com.bitcola.exchange.security.me.constant.EmailCaptchaConstant;
import com.bitcola.exchange.security.me.feign.IPushFeign;
import com.bitcola.exchange.security.me.util.RedisUtil;
import com.bitcola.exchange.security.me.util.SequenceFactory;
import com.bitcola.me.entity.ColaUserEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 公共
 *
 * @author zkq
 * @create 2018-10-09 18:13
 **/
@RestController
@RequestMapping("common")
public class ColaCommonController {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ColaSmsBiz smsBiz;

    @Autowired
    ColaUserBiz userBiz;

    @Autowired
    IPushFeign pushFeign;
    @Autowired
    SequenceFactory sequenceFactory;

    @Autowired
    ColaSecurityCenterController securityCenterController;

    /**
     * 登录后的验证码,有手机则是手机验证码,没有则是邮箱验证码,验证的时候验证两次,先验证
     * @return
     * @throws Exception
     */
    @RequestMapping("captcha")
    public AppResponse captcha(String modules, HttpServletRequest request) throws Exception{
        if (StringUtils.isNotBlank(modules)){
            if(!CaptchaConstant.modules.contains(modules)){
                return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
            }
        }
        String ip = securityCenterController.getIp(request);
        long generate = sequenceFactory.generate("SMS_IP_LIMIT" + ip, 10, TimeUnit.MINUTES);
        if (generate>10){
            return AppResponse.paramsError();
        }
        String userID = BaseContextHandler.getUserID();
        ColaUserEntity info = userBiz.info(userID);
        String areaCode = info.getAreaCode();
        String phoneNumber = info.getTelPhone();
        String email = info.getEmail();
        Integer captcha = new Random().nextInt(899999) + 100000;
        if (StringUtils.isNotBlank(phoneNumber)){
            Object o = redisUtil.get(EmailCaptchaConstant.SMS_CAPTCHA + areaCode + phoneNumber +modules+ "_1minLimit");
            if (o!=null){
                return AppResponse.error(ResponseCode.CAPTCHA_ERROR_CODE,ResponseCode.CAPTCHA_ERROR_MESSAGE);
            }
            pushFeign.sms(captcha.toString(),areaCode,phoneNumber);
            redisUtil.set(EmailCaptchaConstant.SMS_CAPTCHA +areaCode+phoneNumber,captcha.toString(),10L, TimeUnit.MINUTES);
            redisUtil.set(EmailCaptchaConstant.SMS_CAPTCHA +areaCode+phoneNumber+modules+"_1minLimit",captcha.toString(),1L, TimeUnit.MINUTES);
        } else {
            Object o = redisUtil.get(EmailCaptchaConstant.EMAIL_CAPTCHA + email + modules+"_1minLimit");
            if (o!=null){
                return AppResponse.error(ResponseCode.CAPTCHA_ERROR_CODE,ResponseCode.CAPTCHA_ERROR_MESSAGE);
            }
            pushFeign.email(ColaLanguage.getCurrentLanguage(),email,captcha.toString(),userBiz.getAntiPhishingCode());
            // 将验证码放入redis 10分钟有效
            redisUtil.set(EmailCaptchaConstant.EMAIL_CAPTCHA +email,captcha.toString(),10L, TimeUnit.MINUTES);
            redisUtil.set(EmailCaptchaConstant.EMAIL_CAPTCHA +email+modules+"_1minLimit",captcha.toString(),1L, TimeUnit.MINUTES);
        }
        return AppResponse.ok();
    }

}
