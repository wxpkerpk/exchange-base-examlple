package com.bitcola.exchange.security.auth.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author wx
 * @create 2017/12/26.
 */
@Configuration
@Data
public class UserConfiguration {
    @Value("${jwt.token-header}")
    private String userTokenHeader;

    public String getUserTokenHeader() {
        return userTokenHeader;
    }
}
