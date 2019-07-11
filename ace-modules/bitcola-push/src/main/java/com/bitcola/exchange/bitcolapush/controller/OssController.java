package com.bitcola.exchange.bitcolapush.controller;

import com.bitcola.exchange.bitcolapush.oss.OssUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zkq
 * @create 2019-01-03 17:17
 **/
@RestController
@RequestMapping("oss/api")
public class OssController {

    /**
     * 往 OSS 里面放 API
     *
     * @param object    这个对象会转成 json 对象
     * @param apiName   api 名字( 会被命名为 apiName.json 文件),重复的名字会覆盖
     * @return
     */
    @RequestMapping("pushOssApi")
    public boolean pushOssApi(Object object,String apiName){
        if (object == null) return false;
        if (StringUtils.isBlank(apiName)) return false;
        return OssUtil.pushApi(object,apiName);
    }

}
