package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.auth.client.annotation.IgnoreUserToken;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.constant.CaptchaConstant;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.me.biz.ColaUserBiz;
import com.bitcola.exchange.security.me.constant.EmailCaptchaConstant;
import com.bitcola.exchange.security.me.feign.IPushFeign;
import com.bitcola.exchange.security.me.util.RedisUtil;
import com.bitcola.exchange.security.me.util.SequenceFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 邮箱验证码
 *
 * @author zkq
 * @create 2018-07-29 14:54
 **/
@RestController
@RequestMapping("/mail")
public class ColaEmailCaptchaController {


    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ColaUserBiz userBiz;

    @Autowired
    IPushFeign pushFeign;

    @Autowired
    SequenceFactory sequenceFactory;

    @Autowired
    ColaSecurityCenterController securityCenterController;
    /**
     * 发送验证码
     *
     * @author zkq
     * @date 2018/7/29 14:57
     */
    @IgnoreUserToken
    @RequestMapping("captcha")
    public AppResponse Captcha(String email, String modules, HttpServletRequest request) {
        if (StringUtils.isNotBlank(modules)){
            if(!CaptchaConstant.modules.contains(modules)){
                return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
            }
        }
        String ip = securityCenterController.getIp(request);
        long generate = sequenceFactory.generate("EMAIL_IP_LIMIT" + ip, 10, TimeUnit.MINUTES);
        if (generate>10){
            return AppResponse.paramsError();
        }
        Object o = redisUtil.get(EmailCaptchaConstant.EMAIL_CAPTCHA + email + modules+"_1minLimit");
        if (o!=null){
            return AppResponse.error(ResponseCode.CAPTCHA_ERROR_CODE,ResponseCode.CAPTCHA_ERROR_MESSAGE);
        }
        if (email == null){
            String userID = BaseContextHandler.getUserID();
            email = userBiz.info(userID).getEmail();
        }
        Integer captcha = new Random().nextInt(899999) + 100000;
        pushFeign.email(ColaLanguage.getCurrentLanguage(),email,captcha.toString(),"");
        // 将验证码放入redis 10分钟有效
        redisUtil.set(EmailCaptchaConstant.EMAIL_CAPTCHA +email,captcha.toString(),10L, TimeUnit.MINUTES);
        redisUtil.set(EmailCaptchaConstant.EMAIL_CAPTCHA +email+modules+"_1minLimit",captcha.toString(),1L, TimeUnit.MINUTES);
        return new AppResponse();
    }




}
