package com.bitcola.exchange.script;

import com.bitcola.exchange.security.common.constant.UserConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zkq
 * @create 2019-03-26 10:12
 **/
public class ScriptUser {
    public static final String AUTO_MAKE_ORDER_USER = "10";
    public static final String BALANCE_PRICE_USER = "11";
    public static final String NO_MATCH_USER = "NO_MATCH_ID";

    public static final List<String> SCRIPT_USER = new ArrayList<>();
    static {
        SCRIPT_USER.add(AUTO_MAKE_ORDER_USER);
        SCRIPT_USER.add(BALANCE_PRICE_USER);
        SCRIPT_USER.add(UserConstant.SYS_ADMIN);
    }


}
