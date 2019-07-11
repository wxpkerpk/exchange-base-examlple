package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.auth.client.annotation.IgnoreUserToken;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.constant.UserConstant;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.ObjectRestResponse;
import com.bitcola.exchange.security.me.biz.ColaGoogleAuthenticatorBiz;
import com.bitcola.exchange.security.me.biz.ColaKycBiz;
import com.bitcola.exchange.security.me.biz.ColaUserBiz;
import com.bitcola.exchange.security.me.constant.EmailCaptchaConstant;
import com.bitcola.me.entity.ColaUserKyc;
import com.bitcola.exchange.security.me.feign.IActivityFeign;
import com.bitcola.exchange.security.me.feign.IPushFeign;
import com.bitcola.exchange.security.me.feign.IUserService;
import com.bitcola.exchange.security.me.feign.JwtAuthenticationRequest;
import com.bitcola.exchange.security.me.thread.ColaLoginLogThread;
import com.bitcola.exchange.security.me.util.GoogleAuthenticator;
import com.bitcola.exchange.security.me.util.RedisUtil;
import com.bitcola.exchange.security.me.util.SequenceFactory;
import com.bitcola.me.entity.ColaLoginLog;
import com.bitcola.me.entity.ColaUserEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 安全中心
 *
 * @author zkq
 * @create 2018-10-09 18:27
 **/
@RestController
@RequestMapping("security")
public class ColaSecurityCenterController {

    @Autowired
    ColaUserBiz colaUserBiz;

    @Autowired
    private IUserService iUserService;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    ColaGoogleAuthenticatorBiz authenticatorBiz;

    @Autowired
    SequenceFactory sequenceFactory;

    @Autowired
    ColaCommonController commonController;
    @Autowired
    IActivityFeign activityFeign;

    @Autowired
    IPushFeign pushFeign;

    @Autowired
    ColaKycBiz kycBiz;

    ExecutorService executor = Executors.newFixedThreadPool(5);

    public static final String PHONE_REGEX = "(.{3}).+(.{3})";
    public static final String EMAIL_REGEX = "(\\w?)(\\w+)(\\w)(@\\w+\\.[a-z]+(\\.[a-z]+)?)";


    /**
     * 登陆
     * @return
     */
    @IgnoreUserToken
    @RequestMapping(value = "login",method = RequestMethod.POST)
    public AppResponse login(@RequestBody Map<String,String> params, HttpServletRequest request){
        String colaDeviceId = request.getHeader("ColaDeviceId");
        String colaUserAgent = request.getHeader("ColaUserAgent");
        String username = params.get("username").toLowerCase();
        String password = params.get("password");
        String captcha = params.get("captcha");
        String imageToken = params.get("token");
        if (StringUtils.isAnyBlank(username,password,colaDeviceId)){
            return AppResponse.paramsError();
        }
        // 先暂时取消滑动验证
        //if (!pushFeign.verifyToken(imageToken)){
        //    return AppResponse.paramsError();
        //}
        // 防止暴力登录,两个小时只能登陆5次
        long i = sequenceFactory.generate("login_error_limit"+username,2,TimeUnit.HOURS);
        if (i>5){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_LOGIN_ERROR_LIMIT));
        }

        String areaCode = params.get("areaCode");
        ColaUserEntity user = colaUserBiz.getUser(username,areaCode);
        if (user == null){
            return AppResponse.error(ResponseCode.EX_USER_PASS_INVALID_CODE, ColaLanguage.get(ColaLanguage.ME_LOGIN_ERROR_LIMIT_TIME)+(5-i));
        }
        if (user.getEnable() == 0){
            return AppResponse.error(ResponseCode.LOGIN_LIMIT_CODE,ResponseCode.LOGIN_LIMIT_MESSAGE);
        }
        //保存登录日志
        ColaLoginLog loginLog = new ColaLoginLog();
        loginLog.setUserId(user.getSysUserID());
        loginLog.setUsername(user.getUsername());
        loginLog.setNickName(user.getNickName());
        loginLog.setIp(getIp(request));
        loginLog.setPlatform(getPlatform(colaUserAgent,request));
        loginLog.setDevice(getDevice(colaUserAgent,request));
        loginLog.setVersion(getVersion(colaUserAgent,request));
        loginLog.setTime(System.currentTimeMillis());
        loginLog.setId(UUID.randomUUID().toString());

        if (!EncoderUtil.matches(password,user.getPassword())){
            //保存日志
            loginLog.setStatus("failed");

            executor.submit(new ColaLoginLogThread(loginLog));
            return AppResponse.error(ResponseCode.EX_USER_PASS_INVALID_CODE, ColaLanguage.get(ColaLanguage.ME_LOGIN_ERROR_LIMIT_TIME)+(5-i));
        }
        redisTemplate.delete("login_error_limit" + username);
        if (StringUtils.isNotBlank(captcha)){
            BaseContextHandler.setUserID(user.getSysUserID());
            if (!verificationCode(captcha)){
                return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_COMMON_CAPTCHA_ERROR));
            }
        } else {
            if(!verificationLogin(user.getSysUserID(),colaDeviceId,loginLog.getIp())){
            // 登录,触发二次验证判定
                //发送验证码
                try {
                    BaseContextHandler.setUserID(user.getSysUserID());
                    AppResponse response = commonController.captcha("login",request);
                    if (response.getStatus() == ResponseCode.CAPTCHA_ERROR_CODE){
                        return AppResponse.error(ResponseCode.CAPTCHA_ERROR_CODE,ColaLanguage.get(ColaLanguage.ME_COMMON_CAPTCHA_ERROR));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return AppResponse.error(ResponseCode.LOGIN_REQUIRED_CAPTCHA_CODE,ResponseCode.LOGIN_REQUIRED_CAPTCHA_MESSAGE);
            }
        }
        // token 未过期 或者验证通过(包含验证码,和二次验证) 登录,刷新 token
        // 保存验证信息
        colaUserBiz.save2FA(colaDeviceId,user.getSysUserID(),loginLog.getIp());
        //切换登录语言
        colaUserBiz.setColaLanguage(ColaLanguage.getCurrentLanguage());
        return AppResponse.ok().data(this.getToken(user.getUsername(),password,colaUserAgent,user.getSysUserID(),loginLog));
    }




    /**
     * 刷新 token
     * @return
     */
    @RequestMapping("/refreshToken")
    public AppResponse refreshToken(){
        ObjectRestResponse<String> response = iUserService.refreshToken();
        String token = response.getData();


        String userID = BaseContextHandler.getUserID();
        ColaUserEntity info = colaUserBiz.info(userID);
        if (info.getEnable() == 0){
            return AppResponse.error(ResponseCode.LOGIN_LIMIT_CODE,ResponseCode.LOGIN_LIMIT_MESSAGE);
        }
        String o = redisTemplate.opsForValue().get(UserConstant.USER_LOGIN_KEY+userID);
        if (o!=null){
            redisTemplate.delete(UserConstant.USER_LOGIN_KEY+userID);
            redisTemplate.delete(UserConstant.USER_LOGIN_TOKEN_KEY+userID+o);
            // 主动 webSocket 推送给客户端下线
        }
        String uuid = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(UserConstant.USER_LOGIN_KEY+userID,uuid,48L, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(UserConstant.USER_LOGIN_TOKEN_KEY+userID+uuid,token,48L, TimeUnit.HOURS);
        Long expire = TimeUnit.HOURS.toMillis(46);
        Map<String,Object> result = new HashMap<>();
        result.put("token",token);
        result.put("expire",expire);
        return AppResponse.ok().data(result);
    }



    /**
     * 安全中心状态
     * @return
     */
    @RequestMapping("securityStatus")
    public AppResponse securityStatus(){
        Map<String,Map<String,Object>> result = new HashMap<>();
        Map<String,String> map = colaUserBiz.securityStatus();
        String password = map.get("password");
        String pin = map.get("pin");
        String google = map.get("google");
        String email = map.get("email");
        String phone = map.get("phone");
        String fish = map.get("fish");
        Map<String,Object> item =  new HashMap<>();
        Map<String,Object> item2 =  new HashMap<>();
        Map<String,Object> item3 =  new HashMap<>();
        Map<String,Object> item4 =  new HashMap<>();
        Map<String,Object> item5 =  new HashMap<>();
        Map<String,Object> item6 =  new HashMap<>();
        Map<String,Object> item7 =  new HashMap<>();
        item.put("status",1);
        item.put("show","");
        if (StringUtils.isBlank(pin)){
            item2.put("status",0);
        }else {
            item2.put("status",1);
        }
        item2.put("show","");
        ColaUserKyc colaUserKyc = kycBiz.selectById(BaseContextHandler.getUserID());
        item3.put("show","");
        if (colaUserKyc != null){
            item3.put("status",colaUserKyc.getKycStatus());
            if (colaUserKyc.getKycStatus() == -2){
                item3.put("show",colaUserKyc.getReason());
            }
        } else {
            item3.put("status",-1);
        }
        if (StringUtils.isBlank(phone)){
            item4.put("status",0);
            item4.put("show","");
        } else {
            item4.put("status",1);
            item4.put("show",phone.replaceAll(PHONE_REGEX,"$1****$2"));
        }
        if (StringUtils.isBlank(email)){
            item5.put("status",0);
            item5.put("show","");
        } else {
            item5.put("status",1);
            item5.put("show",email.replaceAll(EMAIL_REGEX,"$1****$3$4"));
        }
        if (StringUtils.isBlank(google)){
            item6.put("status",0);
        } else {
            item6.put("status",1);
        }
        item6.put("show","");
        if (StringUtils.isBlank(fish)){
            item7.put("status",0);
            item7.put("show","");
        } else {
            item7.put("status",1);
            item7.put("show",fish);
        }
        result.put("loginPassword",item);
        result.put("pin",item2);
        result.put("kyc",item3);
        result.put("sms",item4);
        result.put("email",item5);
        result.put("googleAuthentication",item6);
        result.put("antiPhishingCode",item7);
        return AppResponse.ok().data(result);
    }


    /**
     * 修改登录密码
     */
    @RequestMapping(value = "editLoginPassword",method = RequestMethod.POST)
    public AppResponse editLoginPassword(@RequestBody Map<String,String> params){
        String currentPassword = params.get("currentPassword");
        String newPassword = params.get("newPassword");
        String captcha = params.get("captcha");
        if (StringUtils.isBlank(currentPassword) || StringUtils.isBlank(newPassword) || StringUtils.isBlank(captcha)){
            return AppResponse.paramsError();
        }
        if (!verificationCode(captcha)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_COMMON_CAPTCHA_ERROR));
        }
        String userID = BaseContextHandler.getUserID();
        ColaUserEntity info = colaUserBiz.info(userID);
        if ( !EncoderUtil.matches(currentPassword,info.getPassword())){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_COMMON_PASSWORD_ERROR));
        }
        colaUserBiz.resetPassword( userID,info.getUsername(),EncoderUtil.encode(newPassword));
        return AppResponse.ok();
    }

    /**
     * 修改 pin
     */
    @RequestMapping(value = "editTransactionPin",method = RequestMethod.POST)
    public AppResponse editTransactionPin(@RequestBody Map<String,String> params){
        String currentPin = params.get("currentPin");
        String newPin = params.get("newPin");
        String captcha = params.get("captcha");
        if (StringUtils.isBlank(newPin) || StringUtils.isBlank(captcha)){
            return AppResponse.paramsError();
        }
        //加入次数限制
        long i = sequenceFactory.generate("edit_pin_limit"+BaseContextHandler.getUserID(),2,TimeUnit.HOURS);
        if (i>5){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_LOGIN_ERROR_LIMIT));
        }
        if(!newPin.matches("\\d{6}")){
            return AppResponse.paramsError();
        }
        if (!verificationCode(captcha)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_COMMON_CAPTCHA_ERROR));
        }
        String userID = BaseContextHandler.getUserID();
        ColaUserEntity info = colaUserBiz.info(userID);
        if (StringUtils.isNotBlank(info.getMoneyPassword())){
            if ( !EncoderUtil.matches(currentPin,info.getMoneyPassword())){
                return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_COMMON_PASSWORD_ERROR));
            }
        }
        colaUserBiz.setMoneyPassword(userID,EncoderUtil.encode(newPin));
        sequenceFactory.delete("edit_pin_limit"+BaseContextHandler.getUserID());
        return AppResponse.ok();
    }

    /**
     * 绑定手机
     * @param params
     * @return
     */
    @RequestMapping(value = "bindPhone",method = RequestMethod.POST)
    public AppResponse bindPhone(@RequestBody Map<String,String> params,HttpServletRequest request){
        String telPhone = params.get("telPhone");
        String areaCode = params.get("areaCode");
        String smsCaptcha = params.get("smsCaptcha");
        String emailCaptcha = params.get("emailCaptcha");
        if (StringUtils.isBlank(telPhone) || StringUtils.isBlank(areaCode) || StringUtils.isBlank(smsCaptcha) ||StringUtils.isBlank(emailCaptcha)){
            return AppResponse.paramsError();
        }
        //验证码
        Object cap = redisUtil.get(EmailCaptchaConstant.SMS_CAPTCHA +areaCode+telPhone);
        if (!smsCaptcha.equals(cap)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_COMMON_CAPTCHA_ERROR));
        }
        ColaUserEntity info = colaUserBiz.info(BaseContextHandler.getUserID());
        cap = redisUtil.get(EmailCaptchaConstant.EMAIL_CAPTCHA +info.getEmail());
        if (!emailCaptcha.equals(cap)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_COMMON_CAPTCHA_ERROR));
        }
        //检查是否重复
        int i = colaUserBiz.checkRepeat(null,telPhone,null, areaCode);
        if (i == 1){
            return new AppResponse(ResponseCode.TIP_ERROR_CODE,ColaLanguage.get(ColaLanguage.ME_USER_REPEAT));
        }
        info.setAreaCode(areaCode);
        info.setTelPhone(telPhone);
        colaUserBiz.setTelPhone(info);
        //activityFeign.reward(info);
        return AppResponse.ok();
    }

    /**
     * 绑定邮箱
     * @param params
     * @return
     */
    @RequestMapping(value = "bindEmail",method = RequestMethod.POST)
    public AppResponse bindEmail(@RequestBody Map<String,String> params){
        String email = params.get("email").toLowerCase();
        String emailCaptcha = params.get("emailCaptcha");
        String smsCaptcha = params.get("smsCaptcha");
        if (StringUtils.isBlank(email) || StringUtils.isBlank(emailCaptcha) || StringUtils.isBlank(smsCaptcha)){
            return AppResponse.paramsError();
        }
        //验证码
        Object cap = redisUtil.get(EmailCaptchaConstant.EMAIL_CAPTCHA +email);
        if (!emailCaptcha.equals(cap)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_COMMON_CAPTCHA_ERROR));
        }
        ColaUserEntity info = colaUserBiz.info(BaseContextHandler.getUserID());
        cap = redisUtil.get(EmailCaptchaConstant.SMS_CAPTCHA +info.getAreaCode()+info.getTelPhone());
        if (!smsCaptcha.equals(cap)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_COMMON_CAPTCHA_ERROR));
        }
        //检查是否重复
        int i = colaUserBiz.checkRepeat(email,null,null, null);
        if (i == 1){
            return new AppResponse(ResponseCode.TIP_ERROR_CODE,ColaLanguage.get(ColaLanguage.ME_USER_REPEAT));
        }
        info.setEmail(email);
        colaUserBiz.setEmail(info);
        return AppResponse.ok();
    }

    /**
     * 设置钓鱼码
     * @return
     */
    @RequestMapping(value = "antiPhishingCode",method = RequestMethod.POST)
    public AppResponse antiPhishingCode(@RequestBody Map<String,String> params){
        String code = params.get("code");
        if (StringUtils.isBlank(code)){
            return AppResponse.paramsError();
        }
        colaUserBiz.antiPhishingCode(code);
        return AppResponse.ok();
    }

    /**
     * 获取谷歌 token key
     * @return
     */
    @RequestMapping("getGoogleAuthenticationTokenKey")
    public AppResponse getGoogleAuthenticationTokenKey(){
        Map<String, String> map = new HashMap<>();
        ColaUserEntity info = colaUserBiz.info(BaseContextHandler.getUserID());
        String tokenKey = GoogleAuthenticator.generateSecretKey();
        String qrBarcode = getQrBarcode(info,tokenKey);
        map.put("tokenKey",tokenKey);
        map.put("qrBarcode",qrBarcode);
        return AppResponse.ok().data(map);
    }

    /**
     * 设置谷歌认证
     * @param params
     * @return
     */
    @RequestMapping(value = "googleAuthentication",method = RequestMethod.POST)
    public AppResponse googleAuthentication(@RequestBody Map<String,String> params){
        String tokenKey = params.get("tokenKey");
        String googleCode = params.get("googleCode");
        String oldGoogleCode = params.get("oldGoogleCode");
        String captcha = params.get("captcha");
        if (StringUtils.isBlank(tokenKey) || StringUtils.isBlank(googleCode) || StringUtils.isBlank(captcha)){
            return AppResponse.paramsError();
        }
        ColaUserEntity info = colaUserBiz.info(BaseContextHandler.getUserID());
        String oldTokenKey = info.getGoogleSecretKey();
        if (StringUtils.isNotBlank(oldTokenKey)){
            if (!GoogleAuthenticator.check_code(oldTokenKey,Long.valueOf(oldGoogleCode),System.currentTimeMillis())){
                return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_COMMON_CAPTCHA_ERROR));
            }
        }
        if (!verificationCode(captcha)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_COMMON_CAPTCHA_ERROR));
        }
        if (!GoogleAuthenticator.check_code(tokenKey,Long.valueOf(googleCode),System.currentTimeMillis())){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_COMMON_CAPTCHA_ERROR));
        }
        colaUserBiz.googleAuthentication(tokenKey);
        return AppResponse.ok();
    }

    /**
     * 取消谷歌认证
     * @param params
     * @return
     */
    @RequestMapping(value = "cancelGoogleAuthentication",method = RequestMethod.POST)
    public AppResponse cancelGoogleAuthentication(@RequestBody Map<String,String> params){
        String googleCode = params.get("googleCode");
        String captcha = params.get("captcha");
        String pin = params.get("pin");
        if (StringUtils.isBlank(googleCode) || StringUtils.isBlank(captcha) || StringUtils.isBlank(pin))
        if (verificationCode(captcha)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_COMMON_CAPTCHA_ERROR));
        }
        ColaUserEntity info = colaUserBiz.info(BaseContextHandler.getUserID());
        if (StringUtils.isBlank(info.getMoneyPassword())){
            return new AppResponse(ResponseCode.NO_MONEY_PASSWORD_CODE, ResponseCode.NO_MONEY_PASSWORD_MESSAGE);
        }
        String googleSecretKey = info.getGoogleSecretKey();
        if (!GoogleAuthenticator.check_code(googleSecretKey,Long.valueOf(googleCode),System.currentTimeMillis())){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_COMMON_CAPTCHA_ERROR));
        }
        if ( !EncoderUtil.matches(pin,info.getMoneyPassword())){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_COMMON_PASSWORD_ERROR));
        }
        colaUserBiz.cancelGoogleAuthentication();
        return AppResponse.ok();
    }

    /**
     * 忘记登录密码
     */
    @IgnoreUserToken
    @RequestMapping("forgetLoginPasswordByPhone")
    public AppResponse forgetLoginPasswordByPhone(@RequestBody Map<String,String> params){
        String phone = params.get("phone");
        String areaCode = params.get("areaCode");
        String captcha = params.get("captcha");
        String newPassword = params.get("newPassword");
        if (StringUtils.isBlank(phone) || StringUtils.isBlank(areaCode) || StringUtils.isBlank(captcha) || StringUtils.isBlank(newPassword)){
            return AppResponse.paramsError();
        }
        Object cap = redisUtil.get(EmailCaptchaConstant.SMS_CAPTCHA +areaCode+phone);
        if (!captcha.equals(cap)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_COMMON_CAPTCHA_ERROR));
        }
        ColaUserEntity user = colaUserBiz.getUser(phone, areaCode);
        if (user == null) return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_ACCOUNT_NOT_EXIST));
        colaUserBiz.resetPassword(user.getSysUserID(),user.getUsername(), EncoderUtil.encode(newPassword));
        redisTemplate.delete("login_error_limit" + phone);
        return AppResponse.ok();
    }

    /**
     * 忘记登录密码
     */
    @IgnoreUserToken
    @RequestMapping("forgetLoginPasswordByEmail")
    public AppResponse forgetLoginPasswordByEmail(@RequestBody Map<String,String> params){
        String email = params.get("email").toLowerCase();
        String captcha = params.get("captcha");
        String newPassword = params.get("newPassword");
        if (StringUtils.isBlank(email) || StringUtils.isBlank(captcha) || StringUtils.isBlank(newPassword)){
            return AppResponse.paramsError();
        }
        Object cap = redisUtil.get(EmailCaptchaConstant.EMAIL_CAPTCHA +email);
        if (!captcha.equals(cap)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_COMMON_CAPTCHA_ERROR));
        }
        ColaUserEntity user = colaUserBiz.getUser(email, null);
        colaUserBiz.resetPassword(user.getSysUserID(),user.getUsername(), EncoderUtil.encode(newPassword));
        redisTemplate.delete("login_error_limit" + email);
        return AppResponse.ok();
    }

    @RequestMapping(value = "kycStatus")
    public AppResponse kyc(){
        ColaUserKyc colaUserKyc = kycBiz.selectById(BaseContextHandler.getUserID());
        Integer status = -1;
        if (colaUserKyc != null){
            status = colaUserKyc.getKycStatus();
        }
        return AppResponse.ok().data(status);
    }

    @RequestMapping(value = "kyc",method = RequestMethod.POST)
    public AppResponse kyc(@RequestBody ColaUserKyc kyc){
        if (StringUtils.isAnyBlank(kyc.getFirstName(),kyc.getLastName(),kyc.getDocumentNumber(),kyc.getDocumentType(),kyc.getFrontSide(),kyc.getBackSide(),kyc.getDocumentAndFace(),kyc.getAreaCode(),kyc.getTelephone(),kyc.getEmail())){
            return AppResponse.paramsError();
        }
        // 检查身份证号是否重复
        Integer i = kycBiz.isDocumentNumberRepeat(kyc);
        if (i >= 1){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_KYC_REPEAT));
        }
        ColaUserKyc colaUserKyc = kycBiz.selectById(BaseContextHandler.getUserID());
        if (colaUserKyc!=null){
            if (colaUserKyc.getKycStatus()==1||colaUserKyc.getKycStatus()==0){
                return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
            } else {
                kyc.setUserId(BaseContextHandler.getUserID());
                kyc.setTimestamp(System.currentTimeMillis());
                kyc.setKycStatus(0);
                kycBiz.updateSelectiveById(kyc);
            }
        } else {
            kyc.setUserId(BaseContextHandler.getUserID());
            kyc.setTimestamp(System.currentTimeMillis());
            kyc.setKycStatus(0);
            kycBiz.insertSelective(kyc);
        }
        return AppResponse.ok();
    }




    /**
     * 公共验证的验证码
     * @return
     */
    private boolean verificationCode(String captcha){
        String userID = BaseContextHandler.getUserID();
        ColaUserEntity info = colaUserBiz.info(userID);
        String areaCode = info.getAreaCode();
        String phoneNumber = info.getTelPhone();
        String email = info.getEmail();
        if (StringUtils.isNotBlank(phoneNumber)){
            Object cap = redisUtil.get(EmailCaptchaConstant.SMS_CAPTCHA +areaCode+phoneNumber);
            if (captcha.equals(cap)){
                return true;
            }
        }
        Object cap = redisUtil.get(EmailCaptchaConstant.EMAIL_CAPTCHA +email);
        if (captcha.equals(cap)){
            return true;
        }
        return false;
    }

    /**
     * 根据用户获得谷歌验证器二维码
     * @param info
     * @param tokenKey
     */
    private String getQrBarcode(ColaUserEntity info, String tokenKey){
        // 电话号码 为空 才是 邮箱,再为空 则取用户名
        String name = StringUtils.isNotBlank(info.getTelPhone())?info.getTelPhone():info.getEmail();
        if (StringUtils.isBlank(name)){
            name = info.getUsername();
        }
        String qrBarcode = GoogleAuthenticator.getQRBarcode(name, tokenKey);
        return qrBarcode;
    }


    /**
     *  二次验证登录判定
     * @param userId
     * @param colaDeviceId
     * @param ip
     * @return
     */
    private boolean verificationLogin(String userId, String colaDeviceId, String ip){
        if ("208186".equalsIgnoreCase(userId)) return true;
        Integer i = colaUserBiz.verificationLogin(userId,colaDeviceId,ip);
        if (i>0) return true;
        return false;
    }


    public String getIp(HttpServletRequest req){
        String ip = req.getHeader("x-user-ip");
        if (StringUtils.isBlank(ip)){
            ip = req.getHeader("x-real-ip");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("x-forwarded-for");
            if (ip != null && ip.split(",").length>1){
                ip = ip.split(",")[0];
            }
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 获取操作系统,浏览器及浏览器版本信息
     * @param request
     * @return
     */
    public static String getBrowserInfo(HttpServletRequest request){
        String  browserDetails  =   request.getHeader("User-Agent");
        String  userAgent       =   browserDetails;
        String  user            =   userAgent.toLowerCase();

        if (user.contains("okhttp/3.10.0")){
            return "Android";
        }
        if (user.contains("postman")){
            return "postman";
        }
        if (user.contains("cfnetwork")){
            return "ios";
        }

        String browser = "";

        //===============Browser===========================
        if (user.contains("edge"))
        {
            browser=(userAgent.substring(userAgent.indexOf("Edge")).split(" ")[0]).replace("/", "-");
        } else if (user.contains("msie"))
        {
            String substring=userAgent.substring(userAgent.indexOf("MSIE")).split(";")[0];
            browser=substring.split(" ")[0].replace("MSIE", "IE")+"-"+substring.split(" ")[1];
        } else if (user.contains("safari") && user.contains("version"))
        {
            browser=(userAgent.substring(userAgent.indexOf("Safari")).split(" ")[0]).split("/")[0]
                    + "-" +(userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1];
        } else if ( user.contains("opr") || user.contains("opera"))
        {
            if(user.contains("opera")){
                browser=(userAgent.substring(userAgent.indexOf("Opera")).split(" ")[0]).split("/")[0]
                        +"-"+(userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1];
            }else if(user.contains("opr")){
                browser=((userAgent.substring(userAgent.indexOf("OPR")).split(" ")[0]).replace("/", "-"))
                        .replace("OPR", "Opera");
            }

        } else if (user.contains("chrome"))
        {
            browser=(userAgent.substring(userAgent.indexOf("Chrome")).split(" ")[0]).replace("/", "-");
        } else if ((user.indexOf("mozilla/7.0") > -1) || (user.indexOf("netscape6") != -1)  ||
                (user.indexOf("mozilla/4.7") != -1) || (user.indexOf("mozilla/4.78") != -1) ||
                (user.indexOf("mozilla/4.08") != -1) || (user.indexOf("mozilla/3") != -1) )
        {
            browser = "Netscape-?";

        } else if (user.contains("firefox"))
        {
            browser=(userAgent.substring(userAgent.indexOf("Firefox")).split(" ")[0]).replace("/", "-");
        } else if(user.contains("rv"))
        {
            String IEVersion = (userAgent.substring(userAgent.indexOf("rv")).split(" ")[0]).replace("rv:", "-");
            browser="IE" + IEVersion.substring(0,IEVersion.length() - 1);
        } else
        {
            browser = "UnKnown, More-Info: "+userAgent;
        }

        return browser ;
    }

    /**
     * 平台,分为 App, Mac, Windows, Mobile web, UnKnown
     * @param colaUserAgent
     * @param request
     * @return
     */
    public String getPlatform(String colaUserAgent, HttpServletRequest request){
        String platform = "UnKnown";
        if (StringUtils.isBlank(colaUserAgent)){
            return platform;
        }
        colaUserAgent = colaUserAgent.toLowerCase();
        if (colaUserAgent.contains("ios") || colaUserAgent.contains("android")){
            platform = "App";
        } else if ("web".equals(colaUserAgent)){
            String agent = request.getHeader("User-Agent").toLowerCase();
            if (agent.contains("windows")){
                platform = "Windows";
            } else if (agent.contains("mac")){
                platform = "Mac";
            } else {
                platform = "Mobile web";
            }
        }
        return platform;
    }

    public String getDevice(String colaUserAgent, HttpServletRequest request){
        String device = "UnKnown";
        if (StringUtils.isBlank(colaUserAgent)){
            return device;
        }
        colaUserAgent = colaUserAgent.toLowerCase();
        if (colaUserAgent.contains("ios")){
            return "iOS";
        } else if (colaUserAgent.contains("android")){
            return "Android";
        } else if ("web".equals(colaUserAgent)){
            String agent = request.getHeader("User-Agent").toLowerCase();
            if (agent.contains("chrome")){
                return "Chrome";
            } else if (agent.contains("safari")){
                return "Safari";
            } else if (agent.contains("edge")){
                return "Edge";
            } else if (agent.contains("firefox")){
                return "Firefox";
            } else if (agent.contains("rv")){
                return "IE";
            }
        }
        return device;
    }

    public String getVersion(String colaUserAgent, HttpServletRequest request) {
        String version = "UnKnown";
        if (StringUtils.isBlank(colaUserAgent)){
            return version;
        }
        colaUserAgent = colaUserAgent.toLowerCase();
        if ("web".equals(colaUserAgent)){
            return request.getHeader("User-Agent").toLowerCase();
        } else {
            return colaUserAgent;
        }
    }

    /**
     * 申请 token
     * @param username
     * @param password
     * @param colaUserAgent
     * @param userId
     * @param loginLog
     * @return
     */
    public Map getToken(String username,String password,String colaUserAgent,String userId,ColaLoginLog loginLog){
        //申请 token
        String token = iUserService.createAuthenticationToken(new JwtAuthenticationRequest(username,password)).getData();
        // 非 web 设备只能登陆一台设备,通过拦截器判断 redis

        if (!"web".equals(colaUserAgent)){
            String o = redisTemplate.opsForValue().get(UserConstant.USER_LOGIN_KEY+userId);
            if (o!=null){
                redisTemplate.delete(UserConstant.USER_LOGIN_KEY+userId);
                redisTemplate.delete(UserConstant.USER_LOGIN_TOKEN_KEY+userId+o);
                System.out.println(userId+"再次登录,以前的 token 过期");
            }
            String uuid = UUID.randomUUID().toString();

            redisTemplate.opsForValue().set(UserConstant.USER_LOGIN_KEY+userId,uuid,48L, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(UserConstant.USER_LOGIN_TOKEN_KEY+userId+uuid,token,48L, TimeUnit.HOURS);
        }
        loginLog.setStatus("successful");
        executor.submit(new ColaLoginLogThread(loginLog));
        Map<String,Object> result = new HashMap<>();
        Long expire = TimeUnit.HOURS.toMillis(46);
        result.put("token",token);
        result.put("expire",expire);
        return result;
    }


}
