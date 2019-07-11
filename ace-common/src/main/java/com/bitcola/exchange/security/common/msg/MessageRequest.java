package com.bitcola.exchange.security.common.msg;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/*
 * @author:wx
 * @description:
 * @create:2018-10-21  17:57
 */
@Data
public class MessageRequest implements Serializable {
    String id;//uuid
    long time;
    String path;//请求路径
    Map<String,Object>params;//参数
    String sign;//签名

}
