package com.bitcola.exchange.security.common.constant;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zkq
 * @create 2018-11-24 17:00
 **/
public class CaptchaConstant {
    public static List<String> modules = new ArrayList<>();
    static {
        modules.add("login");
        modules.add("signUp");
        modules.add("profile");
        modules.add("deposit");
        modules.add("withdraw");
    }
}
