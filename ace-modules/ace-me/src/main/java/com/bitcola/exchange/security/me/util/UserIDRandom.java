package com.bitcola.exchange.security.me.util;

import org.apache.log4j.chainsaw.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 用户 ID 生成
 *
 * @author zkq
 * @create 2018-07-16 19:54
 **/
public class UserIDRandom {

    public static String[] chars = new String[] { "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z" };

    public static String getRandomUserID(){
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 8; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 0x24]);
        }
        return shortBuffer.toString();
    }

    public static String getRandomUserInviterCode(){
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 6; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 0x24]);
        }
        return shortBuffer.toString();
    }


}
