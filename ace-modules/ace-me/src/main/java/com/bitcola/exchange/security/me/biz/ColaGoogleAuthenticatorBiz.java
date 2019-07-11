package com.bitcola.exchange.security.me.biz;

import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.me.util.GoogleAuthenticator;
import com.bitcola.me.entity.ColaUserEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 谷歌验证器
 *
 * @author zkq
 * @create 2018-09-24 18:22
 **/
@Service
public class ColaGoogleAuthenticatorBiz {

    @Autowired
    ColaUserBiz userBiz;


    public Map<String, String> generateSecretKey() {
        Map<String, String> map = new HashMap<>();
        ColaUserEntity info = userBiz.info(BaseContextHandler.getUserID());
        if (StringUtils.isBlank(info.getGoogleSecretKey())){
            String secretKey = GoogleAuthenticator.generateSecretKey();
            info.setGoogleSecretKey(secretKey);
            // 保存密钥
            userBiz.saveSecretKey(info);
        }
        String qrBarcode = getQrBarcode(info);
        map.put("secretKey",info.getGoogleSecretKey());
        map.put("qrBarcode",qrBarcode);
        return map;
    }


    /**
     * 根据用户获得谷歌验证器二维码
     * @param info
     */
    private String getQrBarcode(ColaUserEntity info){
        // 电话号码 为空 才是 邮箱,再为空 则取用户名
        String name = StringUtils.isNotBlank(info.getTelPhone())?info.getTelPhone():info.getEmail();
        if (StringUtils.isBlank(name)){
            name = info.getUsername();
        }
        String qrBarcode = GoogleAuthenticator.getQRBarcode(name, info.getGoogleSecretKey());
        return qrBarcode;
    }


    /**
     * 认证登录用户的谷歌验证码是否正确
     * @param code
     * @return
     */
    public boolean verifySecretKey(long code) {
        ColaUserEntity info = userBiz.info(BaseContextHandler.getUserID());
        if (StringUtils.isBlank(info.getGoogleSecretKey())){
            return false;
        }
        GoogleAuthenticator ga = new GoogleAuthenticator();
        boolean r = ga.check_code(info.getGoogleSecretKey(), code, System.currentTimeMillis());
        return r;
    }
}
