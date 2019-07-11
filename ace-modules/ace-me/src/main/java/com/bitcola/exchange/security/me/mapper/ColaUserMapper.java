package com.bitcola.exchange.security.me.mapper;

import com.bitcola.exchange.security.common.vo.UsersInfoVo;
import com.bitcola.me.entity.ColaUser;
import com.bitcola.me.entity.ColaUserEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 用户表扩展
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:17
 */
@Repository
public interface ColaUserMapper extends Mapper<ColaUser> {

    /**
     * 用户信息
     *
     * @author zkq
     * @date 2018/7/17 15:46
     * @param userID
     * @return ColaUserEntity
     */
    ColaUserEntity info(@Param("userid") String userID);

    Integer getUserId(@Param("username")String username);


    /**
     * 向系统表插入数据
     * @param user
     * @return
     */
    void insertSysUser(ColaUserEntity user);

    /**
     * 新增用户
     * @param user
     */
    void insertUser(ColaUserEntity user);

    /**
     * 检查账号是否已经注册
     *
     * @author zkq
     * @date 2018/7/19 21:07
     * @param email
     * @param telPhone
     * @param username
     * @return int
     */
    int checkRepeat(@Param("email") String email, @Param("telphone") String telPhone, @Param("username")String username,@Param("areacode")String areaCode);

    /**
     * 设置资金密码
     * @param userID
     * @param moneyPassword
     */
    void setMoneyPassword(@Param("userid")String userID, @Param("moneypassword")String moneyPassword);

    /**
     * 设置用户名
     *
     * @author zkq
     * @date 2018/7/29 17:53
     * @param newUsername
     * @param userID
     * @return void
     */
    void resetUsername(@Param("newusername") String newUsername, @Param("userid")String userID);

    /**
     * 从设置登录密码
     * @param userID
     * @param newPassword
     */
    void resetPassword(@Param("userid")String userID, @Param("newpassword")String newPassword);

    /**
     * 设置用户信息
     * @param avatar
     * @param sign
     * @param userID
     * @param nickName
     */
    void setUserInfo(@Param("avatar") String avatar, @Param("sign") String sign, @Param("userid") String userID, @Param("nickname")String nickName);

    /**
     * 查看用户信息
     * @param email
     * @param tel
     * @param username
     * @param areaCode
     * @return
     */
    ColaUserEntity getUser(@Param("email") String email, @Param("tel") String tel, @Param("username") String username, @Param("areacode")String areaCode);

    void saveSecretKey(ColaUserEntity info);

    void setTelPhone(ColaUserEntity info);

    void setEmail(ColaUserEntity info);

    void antiPhishingCode(@Param("userid") String userID, @Param("code") String code);

    String getAntiPhishingCode(@Param("userid") String userID);

    Map<String, String> securityStatus(@Param("userid")String userID);

    void googleAuthentication(@Param("userid") String userID, @Param("tokenkey") String tokenKey);

    void cancelGoogleAuthentication(@Param("userid")String userID);

    Integer getUserIdIndex();

    void updateUserIdIndex();

    int exist2FA(@Param("colaDeviceId")String colaDeviceId, @Param("id")String sysUserID, @Param("ip")String ip);

    void save2FA(@Param("id") String id, @Param("colaDeviceId")String colaDeviceId, @Param("userId")String sysUserID, @Param("ip")String ip);

    Integer verificationLogin(@Param("id")String userId, @Param("colaDeviceId")String colaDeviceId, @Param("ip")String ip);

    List<UsersInfoVo> getUserInfoByIds(@Param("ids")List<String> ids);

    List<UsersInfoVo> searchUser(@Param("keyWord") String keyWord);

    void setColaLanguage(@Param("colaLanguage")String colaLanguage, @Param("userID")String userID);

    List<Map<String, String>> getNoImUser(@Param("ids")List<String> users);

    String getUsername(@Param("id")String userID);
}
