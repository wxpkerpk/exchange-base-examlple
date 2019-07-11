package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.auth.client.annotation.IgnoreClientToken;
import com.bitcola.exchange.security.auth.client.annotation.IgnoreUserToken;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.constant.UserConstant;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.ObjectRestResponse;
import com.bitcola.exchange.security.common.vo.UsersInfoVo;
import com.bitcola.exchange.security.me.biz.ColaCoinBiz;
import com.bitcola.exchange.security.me.biz.ColaUserBiz;
import com.bitcola.exchange.security.me.constant.EmailCaptchaConstant;
import com.bitcola.exchange.security.me.feign.*;
import com.bitcola.exchange.security.me.mapper.ColaLoginLogMapper;
import com.bitcola.exchange.security.me.mapper.ColaUserMapper;
import com.bitcola.exchange.security.me.service.MongoFsService;
import com.bitcola.exchange.security.me.thread.ColaLoginLogThread;
import com.bitcola.exchange.security.me.util.*;
import com.bitcola.exchange.security.me.vo.ImageVo;
import com.bitcola.exchange.security.me.vo.UserInfoVo;
import com.bitcola.me.entity.ColaLoginLog;
import com.bitcola.me.entity.ColaUserEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("colaUser")
public class ColaUserController {

    @IgnoreUserToken
    @IgnoreClientToken
    @RequestMapping("removeLoginLimit")
    public String limit(String email){
        redisTemplate.delete("login_error_limit"+email);
        return "success";
    }

    @Autowired
    ColaSecurityCenterController securityCenterController;

    @Autowired
    ColaUserBiz biz;

    @Autowired
    ColaUserMapper mapper;

    @Autowired
    ColaCoinBiz coinBiz;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ColaLoginLogMapper loginLogMapper;

    @Autowired
    MongoFsService mongoFsService;

    @Autowired
    private IUserService iUserService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    SequenceFactory sequenceFactory;

    @Autowired
    IDataServiceFeign dataServiceFeign;


    @Autowired
    ImageCaptchaUtil imageCaptchaUtil;

    @Autowired
    IPushFeign pushFeign;


    @RequestMapping("getImageCaptcha")
    @IgnoreClientToken
    @IgnoreUserToken
    public AppResponse getImageCaptcha() throws Exception{
        Map<String, Object> generate = imageCaptchaUtil.generate();
        return AppResponse.ok().data(generate);
    }

    @RequestMapping(value = "verificationImageCaptcha",method = RequestMethod.POST)
    @IgnoreClientToken
    @IgnoreUserToken
    public AppResponse verificationImageCaptcha(@RequestBody Map<String,String> params) {
        String key = params.get("key");
        String x = params.get("x");
        return AppResponse.ok().data(imageCaptchaUtil.verification(key,x));
    }


    /**
     * 用户信息
     * @return
     */
    @RequestMapping("/info")
    public AppResponse info(){
        String userID = BaseContextHandler.getUserID();
        ColaUserEntity entity = biz.info(userID);
        AppResponse resp = new AppResponse<>();
        resp.setData(entity);
        return resp;
    }




    /**
     * 注册账号
     *
     * @author zkq
     * @date 2018/7/19 20:41
     * @param captcha   验证码
     * @param type     类型(tel,email)
     * @return com.bitcola.exchange.security.common.msg.BaseResponse
     */
    @IgnoreUserToken
    @RequestMapping(value = "/register",method = RequestMethod.POST)
    public AppResponse register(@RequestBody Map<String,String> params, HttpServletRequest request) throws Exception{
        String type = params.get("type");
        String password = params.get("password");
        String captcha = params.get("captcha");
        String email = params.get("email");
        if (email != null){
            email = email.toLowerCase();
        }
        String inviter = params.get("inviter");
        String telPhone = params.get("telPhone");
        String areaCode = params.get("areaCode");
        String colaUserAgent = request.getHeader("ColaUserAgent");
        String ip = securityCenterController.getIp(request);
        if (StringUtils.isBlank(password) || StringUtils.isBlank(captcha) || StringUtils.isBlank(type)){
            return AppResponse.paramsError();
        }
        if (StringUtils.isBlank(email) && StringUtils.isBlank(telPhone)){
            return AppResponse.paramsError();
        }
        // 先暂时取消滑动验证
        //boolean b = pushFeign.verifyToken(token+"");
        //if (!b) {
        //    return AppResponse.paramsError();
        //}
        //if (!registerIpLimit(ip)){
        //    return AppResponse.paramsError();
        //}
        // 验证码是否正确
        Object cap = null;
        if ("email".equals(type)){
            cap = redisUtil.get(EmailCaptchaConstant.EMAIL_CAPTCHA +email);
        } else {
            cap = redisUtil.get(EmailCaptchaConstant.SMS_CAPTCHA +areaCode+telPhone);
        }
        if (!captcha.equals(cap)){
            return new AppResponse(ResponseCode.TIP_ERROR_CODE,ColaLanguage.get(ColaLanguage.ME_COMMON_CAPTCHA_ERROR));
        }
        int i = biz.checkRepeat(email,telPhone,null, areaCode);
        if (i == 1){
            return new AppResponse(ResponseCode.TIP_ERROR_CODE,ColaLanguage.get(ColaLanguage.ME_REPEAT));
        }
        // 加密密码
        ColaUserEntity user = new ColaUserEntity();
        user.setEmail(email);
        user.setPassword(EncoderUtil.encode(password));
        user.setUsername(UserIDRandom.getRandomUserID()+new Random().nextInt(1000000));
        String randomNickName = UserIDRandom.getRandomUserID();
        user.setNickName(randomNickName);
        //String randomAvatarName = UUID.randomUUID().toString()+".png";
        //mongoFsService.uploadToMongodb(UserAvatarUtil.generateImg(randomNickName),randomAvatarName);
        user.setAvatar(pushFeign.getAvatar(randomNickName));
        user.setTelPhone(telPhone);
        user.setAreaCode(areaCode);
        user.setInviter(inviter!=null?inviter.toUpperCase():null);
        biz.addUser(user,ip);
        // 返回 token
        ColaLoginLog loginLog = new ColaLoginLog();
        loginLog.setUserId(user.getSysUserID());
        loginLog.setUsername(user.getUsername());
        loginLog.setNickName(user.getNickName());
        loginLog.setIp(ip);
        loginLog.setPlatform(securityCenterController.getPlatform(colaUserAgent,request));
        loginLog.setDevice(securityCenterController.getDevice(colaUserAgent,request));
        loginLog.setVersion(securityCenterController.getVersion(colaUserAgent,request));
        loginLog.setTime(System.currentTimeMillis());
        loginLog.setId(UUID.randomUUID().toString());
        biz.setColaLanguage(ColaLanguage.getCurrentLanguage());
        return new AppResponse().data(securityCenterController.getToken(user.getUsername(),password,colaUserAgent,user.getSysUserID(),loginLog));
    }

    /**
     * 限制 IP 注册
     * @param ip
     * @return
     */
    private boolean registerIpLimit(String ip){
        long generate = sequenceFactory.generate("REGISTER_IP_LIMIT" + ip,1,TimeUnit.HOURS);
        if (generate>20) return false;
        return true;
    }


    /**
     * 检测账号是否已经注册
     *
     * @author zkq
     * @date 2018/7/19 21:03
     * @param email
     * @param telPhone
     * @return com.bitcola.exchange.security.common.msg.BaseResponse
     */
    @IgnoreUserToken
    @RequestMapping("checkRepeat")
    public AppResponse checkRepeat(String email,String telPhone,String username,String areaCode){
        if (StringUtils.isBlank(email) && StringUtils.isBlank(telPhone) && StringUtils.isBlank(username)){
            return AppResponse.paramsError();
        }
        if (email!=null){
            email = email.toLowerCase();
        }
        int i = biz.checkRepeat(email,telPhone,username, areaCode);
        if (i > 0){
            return new AppResponse(ResponseCode.TIP_ERROR_CODE,ColaLanguage.get(ColaLanguage.ME_REPEAT));
        }
        return new AppResponse();
    }
    /**
     * 检测账号是否已经存在
     *
     * @author zkq
     * @date 2018/7/19 21:03
     * @param email
     * @param telPhone
     * @return com.bitcola.exchange.security.common.msg.BaseResponse
     */
    @IgnoreUserToken
    @RequestMapping("isExist")
    public AppResponse isExist(String email,String telPhone,String username,String areaCode){
        if (StringUtils.isBlank(email) && StringUtils.isBlank(telPhone) && StringUtils.isBlank(username)){
            return AppResponse.paramsError();
        }
        if (email!=null){
            email = email.toLowerCase();
        }
        int i = biz.checkRepeat(email,telPhone,username, areaCode);
        boolean b = false;
        if (i > 0){
            b = true;
        }
        return new AppResponse().data(b);
    }

    @RequestMapping("hasTransactionPin")
    public AppResponse hasTransactionPin(){
        return AppResponse.ok().data(biz.hasMoneyPassword());
    }


    /**
     *  设置用户名
     *
     * @author zkq
     * @date 2018/7/19 21:53
     * @return com.bitcola.exchange.security.common.msg.BaseResponse
     */
    @RequestMapping(value = "resetUsername",method = RequestMethod.POST)
    public AppResponse  resetUsername(@RequestBody Map<String,String> params){
        String newUsername = params.get("newUsername");
        if (StringUtils.isBlank(newUsername)){
            return AppResponse.paramsError();
        }
        int i = biz.checkRepeat(null,null,newUsername, null);
        if (i == 1){
            return new AppResponse(ResponseCode.TIP_ERROR_CODE,ColaLanguage.get(ColaLanguage.ME_REPEAT));
        }
        biz.resetUsername(newUsername,BaseContextHandler.getUserID());
        return new AppResponse();
    }


    /**
     * 根据id 返回用户信息
     * @return
     */
    @RequestMapping(value = "getUserInfoByIds",method = RequestMethod.POST)
    public AppResponse  getUserInfoByIds(@RequestBody List<String> ids){
        if (ids == null || ids.size() == 0){
            return AppResponse.paramsError();
        }
        List<UsersInfoVo> list = biz.getUserInfoByIds(ids);
        return new AppResponse(list);
    }


    @RequestMapping("search")
    public AppResponse search(String keyWord){
        keyWord = keyWord.toLowerCase();
        List<UsersInfoVo> list = biz.searchUser(keyWord);
        return new AppResponse().data(list);
    }


    /**
     * 设置个人信息
     * @param params
     * @return
     */
    @RequestMapping(value = "setUserInfo",method = RequestMethod.POST)
    public AppResponse  setUserInfo(@RequestBody Map<String,String> params){
        String username = params.get("username");
        String nickName = params.get("nickName");
        String avatar = params.get("avatar");
        String sign = params.get("sign");
        if (dataServiceFeign.contain(username) || dataServiceFeign.contain(nickName) || dataServiceFeign.contain(sign)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_INPUT_LIMIT));
        }
        if (!isOk(username,nickName,sign)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_INPUT_LIMIT));
        }

        if (StringUtils.isNotBlank(username) && !biz.getUsername().equals(username)){
            ColaUserEntity info = biz.info(BaseContextHandler.getUserID());
            if (info.getIsUsernameUpdate() == 1 && !username.equals(info.getUsername())){
                return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_USERNAME_UPDATE_LIMIT));
            }
            int i = biz.checkRepeat(null,null,username, null);
            if (i == 1){
                return new AppResponse(ResponseCode.TIP_ERROR_CODE,ColaLanguage.get(ColaLanguage. ME_REPEAT));
            }
            mapper.resetUsername(username,BaseContextHandler.getUserID());
        }
        if (avatar == null){
            avatar = "";
        }
        if (sign == null){
            sign = "";
        }
        biz.setUserInfo(avatar,sign,nickName);
        return new AppResponse();
    }


    /**
     * 根据用户 ID 获得用户信息
     * @param userId
     * @return
     */
    @IgnoreClientToken
    @IgnoreUserToken
    @RequestMapping("getUserInfoByUserId")
    public AppResponse getUserInfoByUserId(String userId, HttpServletRequest request) throws Exception{
        String token = request.getHeader("Authorization");
        UserInfoVo vo = biz.getUserInfoById(userId,token);
        return AppResponse.ok().data(vo);
    }


    /**
     * 获得最近的照片
     * @param userId
     * @param timestamp
     * @return
     */
    @IgnoreClientToken
    @IgnoreUserToken
    @RequestMapping("getImagesByUserId")
    public AppResponse getImagesByUserId(String userId,Long timestamp,Integer size){
        if (timestamp == null || timestamp == 0){
            timestamp = System.currentTimeMillis();
        }
        if (size == null || size == 0){
            size = 10;
        }
        List<ImageVo> list = biz.getImagesByUserId(userId,timestamp,size);
        return AppResponse.ok().data(list);
    }

    /**
     * 切换语言
     * @return
     */
    @RequestMapping("setColaLanguage")
    public AppResponse setColaLanguage(){
        biz.setColaLanguage(ColaLanguage.getCurrentLanguage());
        return AppResponse.ok();
    }


    private boolean isOk(String... str){
        for (String s : str) {
            s = s.toLowerCase();
            if (s.contains("可乐")) return false;
            if (s.contains("cola")) return false;
            if (s.contains("币可")) return false;
        }
        return true;
    }

    /**
     * 补齐环信上面的所有未注册的用户
     * @return
     */
    @IgnoreUserToken
    @IgnoreClientToken
    @RequestMapping("chatUserInit")
    public AppResponse chatUserInit(){
        Integer i = biz.chatUserInit();
        return AppResponse.ok().data("生成了 "+i+" 个用户");
    }


}