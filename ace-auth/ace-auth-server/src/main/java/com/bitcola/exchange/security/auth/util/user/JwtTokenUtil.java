package com.bitcola.exchange.security.auth.util.user;

import com.bitcola.exchange.security.auth.common.util.jwt.IJWTInfo;
import com.bitcola.exchange.security.auth.common.util.jwt.JWTHelper;
import com.bitcola.exchange.security.auth.configuration.KeyConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by wx on 2017/9/10.
 */
@Component
public class JwtTokenUtil {

    @Value("${jwt.expire}")
    private int expire;
    @Autowired
    private KeyConfiguration keyConfiguration;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public String generateToken(IJWTInfo jwtInfo) throws Exception {
        return JWTHelper.generateToken(jwtInfo, keyConfiguration.getUserPriKey(),expire);
    }

    public IJWTInfo getInfoFromToken(String token) throws Exception {
        return JWTHelper.getInfoFromToken(token, keyConfiguration.getUserPubKey());
    }


}
