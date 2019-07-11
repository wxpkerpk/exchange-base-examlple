package com.bitcola.exchange.security.common.constant;

/**
 * ${DESCRIPTION}
 *
 * @author wx
 * @create 2017-06-14 8:36
 */
public class UserConstant {
    public static int PW_ENCORDER_SALT = 12;
    public static String SYS_ACCOUNT_ID = "8";
    public static String SYS_CTC_ID = "15";
    public static String SYS_ADMIN = "1";

    public static final String DEFAULT_AVATAR = "https://bitcolachina.oss-cn-shanghai.aliyuncs.com/image/default_avatar.png";
    /**
     * 登录的两个 redis key  (key+userId : uuid)
     */
    public static final String USER_LOGIN_KEY = "USER_LOGIN_KEY";
    public static final String USER_LOGIN_TOKEN_KEY = "USER_LOGIN_TOKEN_KEY";

}
