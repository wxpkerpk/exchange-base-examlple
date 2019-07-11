package com.bitcola.exchange.security.common.msg;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/*
 * @author:wx
 * @description:
 * @create:2018-10-21  18:03
 */
@Data
public class RedisResponse implements Serializable {
    String id;//回执请求的id
    String path;
    long time;
    String sign;
   Object data;
    int status;

}
