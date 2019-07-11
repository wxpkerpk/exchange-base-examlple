package com.bitcola.exchange.security.me.biz;

import com.bitcola.community.entity.ArticleItemEntity;
import com.bitcola.community.entity.ImageEntity;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.auth.client.jwt.UserAuthUtil;
import com.bitcola.exchange.security.auth.common.util.jwt.IJWTInfo;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.vo.UsersInfoVo;
import com.bitcola.exchange.security.me.feign.*;
import com.bitcola.exchange.security.me.util.UserIDRandom;
import com.bitcola.exchange.security.me.vo.ImageVo;
import com.bitcola.exchange.security.me.vo.UserInfoVo;
import com.bitcola.me.entity.ColaUser;
import com.bitcola.me.entity.ColaUserEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bitcola.exchange.security.me.mapper.ColaUserMapper;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用户表扩展
 *
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:17
 */
@Service
public class ColaUserBiz extends BaseBiz<ColaUserMapper,ColaUser> {

    @Autowired
    ColaUserClientConfigBiz clientConfigBiz;


    @Autowired
    IActivityFeign activityFeign;

    @Autowired
    IDataServiceFeign dataServiceFeign;

    @Autowired
    ColaCoinBiz coinBiz;

    @Autowired
    UserAuthUtil userAuthUtil;

    @Autowired
    IUserFeignAdminService feignAdminService;

    @Autowired
    IChatFeign chatFeign;


    /**
     * 用户信息
     *
     * @author zkq
     * @date 2018/7/17 15:45
     * @param userID
     * @return ColaUserEntity
     */
    public ColaUserEntity info(String userID) {
        return  mapper.info(userID);
    }


    /**
     * 向系统表插入数据
     * @param user
     * @return
     */
    public void insertSysUser(ColaUserEntity user) {
        mapper.insertSysUser(user);
    }

    /**
     * 新增一个用户
     * @param user
     */
    public void insertUser(ColaUserEntity user) {
        mapper.insertUser(user);
    }

    /**
     * 检查账号是否注册
     *
     * @author zkq
     * @date 2018/7/19 21:06
     * @param email
     * @param telPhone
     * @param username
     * @param areaCode
     * @return int
     */
    public int checkRepeat(String email, String telPhone, String username, String areaCode) {
        return mapper.checkRepeat(email,telPhone,username,areaCode);
    }

    /**
     * 设置资金密码
     *
     * @author zkq
     * @date 2018/7/19 21:46
     * @param userID
     * @param moneyPassword
     * @return void
     */
    public void setMoneyPassword(String userID, String moneyPassword) {
        mapper.setMoneyPassword(userID,moneyPassword);
    }

    /**
     * 设置用户名
     *
     * @author zkq
     * @date 2018/7/29 17:53
     * @param newUsername
     * @param userID
     * @return void
     */
    public void resetUsername(String newUsername, String userID) {
        mapper.resetUsername(newUsername,userID);
    }

    /**
     * 重置登录密码
     * @param newPassword
     */
    public void resetPassword(String userId,String username,String newPassword) {
        feignAdminService.updateLoginPassword(userId,username,newPassword);
    }


    /**
     * 根据 ids 返回用户信息
     * @param ids
     * @return
     */
    public List<UsersInfoVo> getUserInfoByIds(List<String> ids) {
        List<UsersInfoVo> list = mapper.getUserInfoByIds(ids);
        return list;
    }

    @Transactional
    public void setUserInfo( String avatar, String sign, String nickName) {
        String userID = BaseContextHandler.getUserID();
        ColaUserEntity info = mapper.info(userID);
        mapper.setUserInfo(avatar,sign,userID,nickName);
        if (!info.getNickName().equals(nickName)){
            //修改聊天昵称
            chatFeign.update(userID,nickName);
        }
    }

    /**
     * 获得用户信息
     * @param username 用户名,或者邮箱,或者手机号
     * @param areaCode
     * @return
     */
    public ColaUserEntity getUser(String username, String areaCode) {
        //邮箱
        ColaUserEntity user = mapper.getUser(username, null, null, null);
        if (user != null){
            return user;
        }
        //手机号
        user = mapper.getUser(null, username, null,areaCode);
        if (user != null){
            return user;
        }
        //用户名
        user = mapper.getUser(null, null, username, null);
        return user;
    }

    public UserInfoVo getUserInfoById(String userId, String token) throws Exception{
        String selfUserId = null;
        if (StringUtils.isNotBlank(token)){
           IJWTInfo userInfo = userAuthUtil.getInfoFromToken(token);
            selfUserId = userInfo.getId();
        }
        UserInfoVo vo = new UserInfoVo();
        ColaUserEntity info = info(userId);
        vo.setUserId(userId);
        vo.setUsername(info.getUsername());
        vo.setNickName(info.getNickName());
        vo.setSign(info.getSign());
        vo.setAvatar(info.getAvatar());
        // 是否是朋友
        if (StringUtils.isBlank(selfUserId)){
            vo.setIsFriend(0);
            vo.setIsFollowed(0);
        } else {
            // 我是否关注了他
            vo.setIsFollowed(dataServiceFeign.isFollowed(selfUserId,userId));
        }
        // 发表文章多少条
        vo.setPosts(dataServiceFeign.getPostsByUserId(userId));
        // 关注人有多少
        vo.setFollowers(dataServiceFeign.getFollowedByUserId(userId));
        // 粉丝有多少
        vo.setFollowing(dataServiceFeign.getFollowingByUserId(userId));
        return vo;
    }

    public List<ImageVo> getImagesByUserId(String userId, Long timestamp,Integer size) {
        List<ImageVo> list = new ArrayList<>();
        while (list.size() < size){
            ArticleItemEntity entity = dataServiceFeign.getArticleItemEntity(userId,timestamp);
            if (entity!=null){
                List<ImageEntity> images = entity.getImages();
                for (ImageEntity image : images) {
                    ImageVo vo = new ImageVo();
                    BeanUtils.copyProperties(image,vo);
                    vo.setTime(entity.getTime());
                    list.add(vo);
                }
                timestamp = entity.getTime();
            } else {
                return list;
            }
        }
        return list;
    }


    /**
     * 用于判断用户是否有资金密码
     * @return
     */
    public boolean hasMoneyPassword(){
        ColaUserEntity info = info(BaseContextHandler.getUserID());
        return StringUtils.isNotBlank(info.getMoneyPassword());
    }


    public void saveSecretKey(ColaUserEntity info) {
        mapper.saveSecretKey(info);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void addUser(ColaUserEntity user, String ip) {
        user.setWithdrawTime(10);
        mapper.updateUserIdIndex();
        Integer id = mapper.getUserIdIndex();
        user.setTime(System.currentTimeMillis());
        user.setIsUsernameUpdate(0);
        user.setId(id.toString());
        mapper.insertSysUser(user);
        user.setSysUserID(id.toString());
        user.setInviteCode(UserIDRandom.getRandomUserInviterCode());
        user.setSign(ColaLanguage.get(ColaLanguage.ME_USER_SIGN));
        user.setEnable(1);
        mapper.insertUser(user);
        // 生成钱包
        coinBiz.initUserBalance(id.toString());
        clientConfigBiz.insert(id.toString());
        // 生成聊天账号
        //chatFeign.create(id.toString(),user.getNickName());
        user.setIp(ip);
        // 手机号注册则给用户发放奖励
    }

    public void setTelPhone(ColaUserEntity info) {
        mapper.setTelPhone(info);
    }

    public void setEmail(ColaUserEntity info) {
        mapper.setEmail(info);
    }

    public void antiPhishingCode(String code) {
        mapper.antiPhishingCode(BaseContextHandler.getUserID(),code);
    }
    public String getAntiPhishingCode() {
        return mapper.getAntiPhishingCode(BaseContextHandler.getUserID());
    }

    public Map<String, String> securityStatus() {
        return mapper.securityStatus(BaseContextHandler.getUserID());

    }

    public void googleAuthentication(String tokenKey) {
        mapper.googleAuthentication(BaseContextHandler.getUserID(),tokenKey);
    }

    public void cancelGoogleAuthentication() {
        mapper.cancelGoogleAuthentication(BaseContextHandler.getUserID());
    }

    public void save2FA(String colaDeviceId, String sysUserID, String ip) {
        //如果有记录就不保存了
        String id = UUID.randomUUID().toString();
        int i = mapper.exist2FA(colaDeviceId,sysUserID,ip);
        if (i < 1){
            mapper.save2FA(id,colaDeviceId,sysUserID,ip);
        }
    }

    public Integer verificationLogin(String userId, String colaDeviceId, String ip) {
        return mapper.verificationLogin(userId,colaDeviceId,ip);
    }

    public List<UsersInfoVo> searchUser(String keyWord) {
        return mapper.searchUser(keyWord);
    }

    public void setColaLanguage(String colaLanguage) {
        mapper.setColaLanguage(colaLanguage,BaseContextHandler.getUserID());
    }

    public Integer chatUserInit() {
        List<String> users = chatFeign.getUsers();
        // 查询出除此之外的所有用户,然后批量生成
        List<Map<String,String>> list = mapper.getNoImUser(users);
        // 批量注册
        if (list.size()>0){
            chatFeign.createUsers(list);
        }
        return list.size();
    }

    public String getUsername(){
        return mapper.getUsername(BaseContextHandler.getUserID());
    }
}