package com.bitcola.exchange.security.auth.common.util;

import org.springframework.cglib.beans.BeanMap;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.MessageDigest;
import java.util.*;

public class EncoderUtil {

    public static final String BALANCE_KEY = "BALANCE_KEY";
    public static final String WITHDRAW_KEY = "BitColaWithdrawSign";

    private static BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    /**
     * 验证密码
     * @param rawPassword  明文
     * @param encodedPassword  密文
     * @return
     */
    public static boolean matches(CharSequence rawPassword, String encodedPassword){
        return encoder.matches(rawPassword,encodedPassword);
    }

    /**
     * 加密密码
     * @param rawPassword 明文
     * @return
     */
    public static String encode(CharSequence rawPassword) {
        return encoder.encode(rawPassword);
    }


    /**
     * 参数签名
     * @param params
     * @return
     */
    public static String makeSign(Map params){
        System.out.println(EncoderUtil.paramsSort(params));
        return EncoderUtil.MD5(EncoderUtil.paramsSort(params));
    }

    public static String makeSign(Object bean){
        return EncoderUtil.makeSign(BeanMap.create(bean));
    }

    /**
     * 校验参数签名
     * @param params
     * @param hashText
     * @return
     */
    public static boolean checkSign(Map params,String hashText){
        return hashText.equals(EncoderUtil.MD5(EncoderUtil.paramsSort(params)));
    }

    /**
     * 校验参数签名
     * @param bean 需要将bean 中的签名字段设置为 null
     * @param hashText
     * @return
     */
    public static boolean checkSign(Object bean,String hashText){
        return EncoderUtil.checkSign(BeanMap.create(bean),hashText);
    }

    /**
     * 参数排序
     * @param params
     * @return
     */
    public static String paramsSort(Map params){
        Set<String> strings = params.keySet();
        List<String> p = new ArrayList<>(strings);
        Collections.sort(p);
        StringBuilder sb = new StringBuilder();
        for (String  key : p) {
            sb.append("&").append(key).append("=").append(params.get(key));
        }
        return sb.toString().replaceFirst("&","").toLowerCase().replace("_","").replace("0","");
    }


    private static String MD5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.getBytes("utf-8"));
            return toHex(bytes);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHex(byte[] bytes) {
        final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i=0; i<bytes.length; i++) {
            ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
            ret.append(HEX_DIGITS[bytes[i] & 0x0f]);
        }
        return ret.toString();
    }




}
