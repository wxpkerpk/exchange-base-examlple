package com.bitcola.exchange.security.auth.client.i18n;

import java.util.HashMap;
import java.util.Map;

/**
 * 国际化语言
 *      用法示例 String test = ColaLanguage.get(ColaLanguage.ME_ICO_INFO_ERROR);
 *
 * @author zkq
 * @create 2018-09-29 10:55
 **/
public class ColaLanguage {
    private static Map<String, String> EN = new HashMap<>();
    private static Map<String, String> CN = new HashMap<>();
    public static final String LANGUAGE_CN = "CN";
    public static final String LANGUAGE_EN = "EN";

     /**
      * 取值的 key  板块_模块_功能_描述 (采用这种命名)
      */
    public static final String CURRENT_LANGUAGE = "CURRENT_LANGUAGE";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String SYSTEM_BUSY = "SYSTEM_BUSY";
    public static final String VERIFY_FAILED = "VERIFY_FAILED";


    public static final String ME_USER_SIGN = "ME_USER_SIGN";
    public static final String ME_USER_REPEAT = "ME_USER_REPEAT";
    public static final String ME_ICO_INFO_ERROR = "ME_ICO_INFO_ERROR";
    public static final String ME_ICO_ADDRESS_ERROR = "ME_ICO_ADDRESS_ERROR";
    public static final String ME_ICO_SUBSCRIBE = "ME_ICO_SUBSCRIBE";
    public static final String ME_BALANCE_NOT_ENOUGH = "ME_BALANCE_NOT_ENOUGH";
    public static final String ME_COMMON_CAPTCHA_ERROR = "ME_COMMON_CAPTCHA_ERROR";
    public static final String ME_COMMON_PASSWORD_ERROR = "ME_COMMON_PASSWORD_ERROR";
    public static final String ME_WORD_ORDER_COMPLETE = "ME_WORD_ORDER_COMPLETE";
    public static final String ME_LOGIN_ERROR_LIMIT = "ME_LOGIN_ERROR_LIMIT";
    public static final String ME_LOGIN_ERROR_LIMIT_TIME = "ME_LOGIN_ERROR_LIMIT_TIME";
    public static final String ME_INPUT_LIMIT = "ME_INPUT_LIMIT";
    public static final String ME_COIN_WITHDRAW_LIMIT = "ME_COIN_WITHDRAW_LIMIT";
    public static final String ME_COIN_DEPOSIT_LIMIT = "ME_COIN_DEPOSIT_LIMIT";
    public static final String ME_COIN_WITHDRAW_ERROR = "ME_COIN_WITHDRAW_ERROR";
    public static final String ME_REPEAT = "ME_REPEAT";
    public static final String ME_KYC_REPEAT = "ME_KYC_REPEAT";
    public static final String ME_WITHDRAW_DALLY_LIMIT = "ME_WITHDRAW_DALLY_LIMIT";
    public static final String ME_WITHDRAW_DALLY_NUMBER_LIMIT = "ME_WITHDRAW_DALLY_NUMBER_LIMIT";
    public static final String ME_USERNAME_UPDATE_LIMIT = "ME_USERNAME_UPDATE_LIMIT";
    public static final String ME_NEW_COIN_APPLY_LENGTH = "ME_NEW_COIN_APPLY_LENGTH";
    public static final String ME_NEW_COIN_APPLY = "ME_NEW_COIN_APPLY";
    public static final String ME_NEW_COIN_APPLY_LIMIT = "ME_NEW_COIN_APPLY_LIMIT";
    public static final String ME_WITHDRAW_USER_LIMIT = "ME_WITHDRAW_USER_LIMIT";
    public static final String ME_ACCOUNT_NOT_EXIST = "ME_ACCOUNT_NOTEXIST";

    public static final String COMMUNITY_LIKED = "COMMUNITY_LIKED";
    public static final String COMMUNITY_FOLLOWED = "COMMUNITY_FOLLOWED";
    public static final String COMMUNITY_FOLLOWED_SELF = "COMMUNITY_FOLLOWED_SELF";
    public static final String COMMUNITY_NOT_FOLLOWED = "COMMUNITY_NOT_FOLLOWED";
    public static final String COMMUNITY_USER_LIMIT = "COMMUNITY_USER_LIMIT";
    public static final String COMMUNITY_PUBLISH_LIMIT = "COMMUNITY_PUBLISH_LIMIT";

    public static final String CHAT_FRIEND_APPLY = "CHAT_FRIEND_APPLY";

    public static final String EXCHANGE_MAKE_ORDER_LIMIT = "EXCHANGE_MAKE_ORDER_LIMIT";
    public static final String LAUNCHPAD_BUY_BUSY = "LAUNCHPAD_BUY_BUSY";
    public static final String LAUNCHPAD_OVER = "LAUNCHPAD_OVER";
    public static final String LAUNCHPAD_BUY_MAX_LIMIT = "LAUNCHPAD_BUY_MAX_LIMIT";
    public static final String EXCHANGE_MAX_LIMIT = "EXCHANGE_MAX_LIMIT";
    public static final String EXCHANGE_MIN_LIMIT = "EXCHANGE_MIN_LIMIT";
    public static final String CONSUMER_NEED_HELP = "CONSUMER_NEED_HELP";
    public static final String CONSUMER_DEFAULT_ONE = "CONSUMER_DEFAULT_ONE";
    public static final String CONSUMER_DEFAULT_TWO = "CONSUMER_DEFAULT_TWO";
    public static final String CONSUMER_DEFAULT_THREE = "CONSUMER_DEFAULT_THREE";

    public static final String STRING_LIMIT = "STRING_LIMIT";


    /**
     * 中文国际化
     */
    static{
        CN.put(CURRENT_LANGUAGE,"CN");
        CN.put(FORBIDDEN,"非法操作");
        CN.put(SYSTEM_BUSY,"系统繁忙");
        CN.put(VERIFY_FAILED,"验证失败，请重新输入");

        CN.put(ME_USER_SIGN,"这个用户太懒了，什么都没有留下。");
        CN.put(ME_USER_REPEAT,"您当前邮箱或手机已经被绑定，请尝试重新输入");
        CN.put(ME_ICO_INFO_ERROR,"个人信息输入错误，请重新输入");
        CN.put(ME_ICO_ADDRESS_ERROR,"ETH 地址已经存在，请检查该 ETH 地址是否为您本人的地址");
        CN.put(ME_ICO_SUBSCRIBE,"您已经订阅了。");
        CN.put(ME_BALANCE_NOT_ENOUGH,"当前可用余额不足");
        CN.put(ME_COMMON_CAPTCHA_ERROR,"验证码输入错误");
        CN.put(ME_COMMON_PASSWORD_ERROR,"密码错误，请重新输入");
        CN.put(ME_WORD_ORDER_COMPLETE,"当前工单已经结束，请再次提交工单");
        CN.put(ME_LOGIN_ERROR_LIMIT,"当前密码错误次数已达上限，请2小时后重新尝试");
        CN.put(ME_LOGIN_ERROR_LIMIT_TIME,"用户名或密码错误，您还可以尝试的次数为 ");
        CN.put(ME_INPUT_LIMIT,"您输入的内容不正确，请重新输入");
        CN.put(ME_COIN_WITHDRAW_LIMIT,"当前币种无法提币");
        CN.put(ME_COIN_DEPOSIT_LIMIT,"当前币种无法充值");
        CN.put(ME_COIN_WITHDRAW_ERROR,"提币失败，请稍后再试");
        CN.put(ME_REPEAT,"输入内容重复");
        CN.put(ME_KYC_REPEAT,"该证件已被注册");
        CN.put(ME_WITHDRAW_DALLY_LIMIT,"当日提币次数已达上限");
        CN.put(ME_WITHDRAW_DALLY_NUMBER_LIMIT,"当日提币数量已达上限");
        CN.put(ME_USERNAME_UPDATE_LIMIT,"用户名无法被修改");
        CN.put(ME_NEW_COIN_APPLY_LENGTH,"您输入的内容超出最大长度限制，请重新输入！");
        CN.put(ME_NEW_COIN_APPLY,"申请已提交，审核通过后我们将以短信或邮件的方式通知您！");
        CN.put(ME_NEW_COIN_APPLY_LIMIT,"您的申请正在审核中，请不要重复申请");
        CN.put(ME_WITHDRAW_USER_LIMIT,"您的账号已被禁止提现，请提交工单处理或联系管理员进行处理");
        CN.put(ME_ACCOUNT_NOT_EXIST,"当前账号不存在");

        CN.put(COMMUNITY_LIKED,"点赞成功");
        CN.put(COMMUNITY_FOLLOWED,"已关注");
        CN.put(COMMUNITY_NOT_FOLLOWED,"您还没有关注他（她）");
        CN.put(COMMUNITY_FOLLOWED_SELF,"非法操作");
        CN.put(COMMUNITY_USER_LIMIT,"您因发布非法内容已被限制当前功能的使用，将于 %s 后恢复，请您遵守微文发布相关规定");
        CN.put(COMMUNITY_PUBLISH_LIMIT,"您今日发表内容次数已达上限，请明日再试");


        CN.put(CHAT_FRIEND_APPLY,"对方通过了你的好友请求，现在可以开始聊天了。");

        CN.put(EXCHANGE_MAKE_ORDER_LIMIT,"您的账号已被禁止交易");
        CN.put(LAUNCHPAD_BUY_BUSY,"当前购买人数较多，请稍后查看购买是否成功");
        CN.put(LAUNCHPAD_OVER,"已售罄");
        CN.put(LAUNCHPAD_BUY_MAX_LIMIT,"购买总数超过最大限制");

        CN.put(EXCHANGE_MAX_LIMIT,"下单最大限额：%s");
        CN.put(EXCHANGE_MIN_LIMIT,"下单最小限额：%s");
        CN.put(CONSUMER_NEED_HELP,"我需要帮助");
        CN.put(CONSUMER_DEFAULT_ONE,"由于客服人员繁忙，您的问题会在稍后处理，请耐心等待");
        CN.put(CONSUMER_DEFAULT_TWO,"我们正在为您安排客服人员，请稍候");
        CN.put(CONSUMER_DEFAULT_THREE,"已为您安排客服人员，当前排队人数1，请稍候");
        CN.put(STRING_LIMIT,"超出长度限制");
    }

    /**
     * 默认英文国际化
     */
    static{
        EN.put(CURRENT_LANGUAGE,"EN");
        EN.put(FORBIDDEN,"Forbidden");
        EN.put(SYSTEM_BUSY,"System busy");
        EN.put(VERIFY_FAILED,"Verify failed");

        EN.put(ME_USER_SIGN,"This user is too lazy to leave nothing.");
        EN.put(ME_USER_REPEAT,"The content you entered is being used, please try to re-enter");
        EN.put(ME_ICO_INFO_ERROR,"Incorrect input, please check and try again");
        EN.put(ME_ICO_ADDRESS_ERROR,"The ETH address already exists. Please check if the ETH address is your own address.");
        EN.put(ME_ICO_SUBSCRIBE,"You have already subscribed.");
        EN.put(ME_BALANCE_NOT_ENOUGH,"Not enough money");
        EN.put(ME_COMMON_CAPTCHA_ERROR,"Verification code error");
        EN.put(ME_COMMON_PASSWORD_ERROR,"Password error");
        EN.put(ME_WORD_ORDER_COMPLETE,"The work order is complete. Please bring up another work order.");
        EN.put(ME_LOGIN_ERROR_LIMIT,"The current password error has reached the upper limit. Please try again in 2 hours.");
        EN.put(ME_LOGIN_ERROR_LIMIT_TIME,"The username or password is incorrect. The number of times you can try is ");
        EN.put(ME_INPUT_LIMIT,"The content you entered is illegal, please re-enter");
        EN.put(ME_COIN_WITHDRAW_LIMIT,"Coin can`t withdraw");
        EN.put(ME_COIN_DEPOSIT_LIMIT,"Coin can`t deposit");
        EN.put(ME_COIN_WITHDRAW_ERROR,"Failed to withdraw coins, please submit work order acceptance");
        EN.put(ME_REPEAT,"Repeat");
        EN.put(ME_KYC_REPEAT,"The document has been registered");
        EN.put(ME_WITHDRAW_DALLY_LIMIT,"The number of withdraw has been capped on the day");
        EN.put(ME_WITHDRAW_DALLY_NUMBER_LIMIT,"The amount of withdraw has been capped on the day");
        EN.put(ME_USERNAME_UPDATE_LIMIT,"Username is not allow change");
        EN.put(ME_NEW_COIN_APPLY_LENGTH,"The content you submitted may exceed the length, please double check and try again!");
        EN.put(ME_NEW_COIN_APPLY,"The application has been submitted, and we will notify you by SMS and email through the review!");
        EN.put(ME_NEW_COIN_APPLY_LIMIT,"Your application is under review. Please do not repeat the application.");
        EN.put(ME_WITHDRAW_USER_LIMIT,"Your account has been banned from withdraw. If you need to withdraw, please initiate work order processing.");
        EN.put(ME_ACCOUNT_NOT_EXIST,"Your account does not exist, please try to register");

        EN.put(COMMUNITY_LIKED,"You have already praised it");
        EN.put(COMMUNITY_FOLLOWED,"You have already follow this person");
        EN.put(COMMUNITY_FOLLOWED_SELF,"You can't follow to yourself");
        EN.put(COMMUNITY_NOT_FOLLOWED,"You haven't follow this person");
        EN.put(COMMUNITY_USER_LIMIT,"Your use of the current feature has been restricted due to the release of illegal content. It will be restored after %s. Please follow the regulations on micro-article.");
        EN.put(COMMUNITY_PUBLISH_LIMIT,"You have too much article to post today, please try again tomorrow.");

        EN.put(CHAT_FRIEND_APPLY,"I passed your friend verification request and now we can start chatting");

        EN.put(EXCHANGE_MAKE_ORDER_LIMIT,"Your account has been banned from trading");
        EN.put(LAUNCHPAD_BUY_BUSY,"The current purchase is large, please check if the purchase is successful later.");
        EN.put(LAUNCHPAD_OVER,"Currently sold out");
        EN.put(LAUNCHPAD_BUY_MAX_LIMIT,"Total purchases exceed the maximum limit");

        EN.put(EXCHANGE_MAX_LIMIT,"Order maximum limit : %s");
        EN.put(EXCHANGE_MIN_LIMIT,"Order minimum limit : %s");
        EN.put(CONSUMER_NEED_HELP,"I need help");
        EN.put(CONSUMER_DEFAULT_ONE,"Due to the busy customer service, your problem will be handled later, please be patient");
        EN.put(CONSUMER_DEFAULT_TWO,"We are arranging customer service for you, please wait");
        EN.put(CONSUMER_DEFAULT_THREE,"Customer service staff has been arranged for you, the current queue number is 1, please wait");
        CN.put(STRING_LIMIT,"Exceeded length limit");
    }



    /**
     * 获取当前语言
     * @return
     */
    public static String getCurrentLanguage(){
        Map<String, String> map = threadLocal.get();
        if (map == null) {
            threadLocal.set(EN);
            map = EN;
        }
        return map.get(CURRENT_LANGUAGE);
    }


    private static ThreadLocal<Map<String, String>> threadLocal = new ThreadLocal<Map<String, String>>();
    public static void setCN(){
        threadLocal.set(CN);
    }

    public static void setEN(){
        threadLocal.set(EN);
    }

    public static void set(String key, String value) {
        Map<String, String> map = threadLocal.get();
        if (map == null) {
            map = new HashMap<String, String>();
            threadLocal.set(map);
        }
        map.put(key, value);
    }

    public static String get(String key){
        Map<String, String> map = threadLocal.get();
        if (map == null) {
            threadLocal.set(EN);
            map = EN;
        }
        return map.get(key);
    }

}
