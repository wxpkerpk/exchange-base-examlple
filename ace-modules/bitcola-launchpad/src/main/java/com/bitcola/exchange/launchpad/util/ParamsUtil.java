package com.bitcola.exchange.launchpad.util;

import java.util.*;

/**
 * @author zkq
 * @create 2019-03-13 15:38
 **/
public class ParamsUtil {

    private static final String libel = "&&&&&";
    private static final String attributes = "=====";
    public static final String COMMA_5_SPLIT = ",,,,,";
    public static final String COMMA_SPLIT = ",";

    public static String mapToString(Map<String,String> params){
        StringBuilder sb = new StringBuilder();
        for (String key : params.keySet()) {
            sb.append(libel).append(key).append(attributes).append(params.get(key));
        }
        return sb.toString().replaceFirst(libel,"");
    }

    public static Map<String,String> toMap(String params){
        Map<String,String> map = new HashMap<>();
        String[] libels = params.split(libel);
        for (String str : libels) {
            String[] split = str.split(attributes);
            if (split.length == 2){
                map.put(split[0],split[1]);
            } else {
                map.put(split[0],null);
            }
        }
        return map;
    }
    public static String listToString(List<String> params, String split){
        StringBuilder sb = new StringBuilder();
        for (String param : params) {
            sb.append(split).append(param);
        }
        return sb.toString().replaceFirst(split,"");
    }
    public static List<String> toList(String params, String split){
        return new ArrayList<>(Arrays.asList(params.split(split)));
    }


}
