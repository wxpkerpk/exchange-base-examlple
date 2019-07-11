package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.me.biz.ColaGoogleAuthenticatorBiz;
import com.bitcola.exchange.security.me.biz.ColaUserBiz;
import com.bitcola.me.entity.ColaUserEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 谷歌验证器
 *
 * @author zkq
 * @create 2018-09-24 18:20
 **/
@Deprecated
@RestController
@RequestMapping("colaGoogle")
public class ColaGoogleAuthenticatorController {


    @Autowired
    ColaUserBiz userBiz;

    @Resource
    ColaGoogleAuthenticatorBiz authenticatorBiz;

    /**
     * 为当前用户生成谷歌密钥 (移到了安全中心)
     * @return
     */
    @Deprecated
    @RequestMapping("generateSecretKey")
    public AppResponse generateSecretKey(){
        // 谷歌密钥只显示一次,生成之后就不再显示了
        ColaUserEntity info = userBiz.info(BaseContextHandler.getUserID());
        if (StringUtils.isNotBlank(info.getGoogleSecretKey())){
            return AppResponse.error("Google Secret Key has been generated");
        }
        Map<String,String> result = authenticatorBiz.generateSecretKey();
        return AppResponse.ok().data(result);
    }


    /**
     * 认证密钥  (移到了安全中心)
     * @return
     */
    @Deprecated
    @RequestMapping("verifySecretKey")
    public AppResponse verifySecretKey(long code){
        boolean b = authenticatorBiz.verifySecretKey(code);
        return AppResponse.ok().data(b);
    }







}
