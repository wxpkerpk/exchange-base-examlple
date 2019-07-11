package com.bitcola.chain.util;

import org.springframework.util.StringUtils;

/**
 * @author zkq
 * @create 2019-01-19 14:49
 **/
public class PathUtil {

    public static String pathConcat(String parentPath, String path) {
        var currentPath = new StringBuilder(parentPath.toLowerCase()).append("/").append(path.toLowerCase());
        var currentPathStr = currentPath.toString();
        String[] strings = currentPathStr.split("/");
        currentPath = new StringBuilder();
        for (String actor : strings) {
            if (!StringUtils.isEmpty(actor)) {
                currentPath.append("/").append(actor);
            }
        }
        return currentPath.toString().replaceFirst("/","");
    }
}
