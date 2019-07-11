package com.bitcola.exchange.security.common.constant;

/**
 * APP 返回状态码
 *
 * @author zkq
 * @create 2018-08-11 22:47
 **/
public class ResponseCode {


    /**
     * 成功
     */
    public static final int SUCCESS_CODE = 200;
    public static final String SUCCESS_MESSAGE = "success";

    /**
     * 未登录,或者 token 过期
     */
    public static final int TOKEN_ERROR_CODE = 40101;
    public static final String TOKEN_ERROR_MESSAGE = "token error";

    /**
     * 账号被限制登录
     */
    public static final int LOGIN_LIMIT_CODE = 40103;
    public static final String LOGIN_LIMIT_MESSAGE = "Account restriction login";

    /**
     * 登录需要二次验证
     */
    public static final int LOGIN_REQUIRED_CAPTCHA_CODE = 40102;
    public static final String LOGIN_REQUIRED_CAPTCHA_MESSAGE = "required captcha";


    /**
     * 账号密码验证错误
     */
    public static final int EX_USER_PASS_INVALID_CODE = 40001;
    public static final String EX_USER_PASS_INVALID_MESSAGE = "username or password error";
    /**
     * pin 错误
     */
    public static final int PIN_ERROR_CODE = 40002;
    public static final String PIN_ERROR_MESSAGE = "pin error";

    /**
     * 账号限制
     */
    public static final int USER_LIMIT_CODE = 40003;
    public static final String USER_LIMIT_MESSAGE = "Forbidden";


    /**
     * 提示错误 (请返回提示信息)
     */
    public static final int TIP_ERROR_CODE = 900;
    public static final String TIP_ERROR_MESSAGE = "";

    /**
     * 参数错误
     */
    public static final int PARAMS_ERROR_CODE = 700;
    public static final String PARAMS_ERROR_MSG = "params error";

    public static final int CAPTCHA_ERROR_CODE = 701;
    public static final String CAPTCHA_ERROR_MESSAGE = "验证码已经发送，请不要重复发送";

    public static final int CAPTCHA_INVALID_ERROR_CODE = 702;
    public static final String CAPTCHA_INVALID_ERROR_MESSAGE = "captcha";

    /**
     * 邮箱未验证
     */
    public static final int EMAIL_NOT_CONFIRM_CODE = 600;
    public static final String EMAIL_NOT_CONFIRM_MESSAGE = "email not confirm";

    /**
     * 没有设置资金密码
     */
    public static final int NO_MONEY_PASSWORD_CODE = 601;
    public static final String NO_MONEY_PASSWORD_MESSAGE = "not set pin";

    /**
     * 没有通过 kyc
     */
    public static final int NOT_PASS_KYC_CODE = 602;
    public static final String NOT_PASS_KYC_MESSAGE = "not pass kyc";


    /**
     * 服务器错误
     */
    public static final int ERROR_CODE = 500;
    public static final String ERROR_MESSAGE = "server error";

    /**
     * 交易错误
     */
    public static final int NO_ENOUGH_MONEY_CODE = 800;
    public static final String NO_ENOUGH_MONEY_MESSAGE = "no enough money";

    //交易签名过期
    public static final int SIGN_EXPIRE = 801;
    public static final String SIGN_EXPIRE_MESSAGE = "sign expire";
    //交易签名错误
    public static final int SIGN_WRONG = 802;
    public static final String SIGN_WRONG_MESSAGE = "sign wrong";


    /**
     * 币种无法充值
     */
    public static final int COIN_NOT_DEPOSIT_CODE = 901;
    public static final String COIN_NOT_DEPOSIT_MESSAGE = "coin not deposit";

    /**
     * 币种无法提币
     */
    public static final int COIN_NOT_WITHDRAW_CODE = 902;
    public static final String COIN_NOT_WITHDRAW_MESSAGE = "coin not withdraw";

    public static final int COIN_NOT_OPEN=988;
    public static final String COIN_NOT_OPEN_MESSAGE="pair not open";

    public static final int TIME_EXPIRE=989;
    public static final String TIME_EXPIRE_MESSAGE="time expire";



}
