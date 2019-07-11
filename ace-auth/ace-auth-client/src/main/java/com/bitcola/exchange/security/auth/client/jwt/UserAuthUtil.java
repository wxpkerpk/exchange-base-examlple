package com.bitcola.exchange.security.auth.client.jwt;

import com.bitcola.exchange.security.auth.client.config.UserAuthConfig;
import com.bitcola.exchange.security.auth.common.util.jwt.IJWTInfo;
import com.bitcola.exchange.security.auth.common.util.jwt.JWTHelper;
import com.bitcola.exchange.security.common.exception.auth.UserTokenException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * Created by wx on 2017/9/15.
 */
@Configuration
public class UserAuthUtil {
    @Resource(name = "userAuthConfig")
    private UserAuthConfig userAuthConfig;
    public IJWTInfo getInfoFromToken(String token) throws Exception {
        try {
            return JWTHelper.getInfoFromToken(token, userAuthConfig.getPubKeyByte());
        }catch (ExpiredJwtException ex){
            throw new UserTokenException("User token expired!");
        }catch (SignatureException ex){
            throw new UserTokenException("User token signature error!");
        }catch (IllegalArgumentException ex){
            throw new UserTokenException("User token is null or empty!");
        }
    }
}
