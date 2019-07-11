package com.bitcola.exchange.security.auth.client.interceptor;

import com.bitcola.exchange.security.auth.client.config.ServiceAuthConfig;
import com.bitcola.exchange.security.auth.client.config.UserAuthConfig;
import com.bitcola.exchange.security.auth.client.jwt.ServiceAuthUtil;
import com.bitcola.exchange.security.common.constant.CommonConstants;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import lombok.extern.java.Log;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * @author wx
 */
@Component
@Log
public class OkHttpTokenInterceptor implements Interceptor {
    @Autowired
    @Lazy
    private ServiceAuthUtil serviceAuthUtil;
    @Autowired
    @Lazy
    private ServiceAuthConfig serviceAuthConfig;
    @Autowired
    @Lazy
    private UserAuthConfig userAuthConfig;


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request newRequest = null;
        if (chain.request().url().toString().contains("client/token")) {
            newRequest = chain.request()
                    .newBuilder()
                    .header(userAuthConfig.getTokenHeader(), BaseContextHandler.getToken())
                    .header("ColaUserAgent", "web")
                    .build();
        } else {
            newRequest = chain.request()
                    .newBuilder()
                    .header(userAuthConfig.getTokenHeader(), BaseContextHandler.getToken())
                    .header(serviceAuthConfig.getTokenHeader(), serviceAuthUtil.getClientToken())
                    .header("ColaUserAgent", "web")
                    .build();
        }
        Response response = chain.proceed(newRequest);
        if (HttpStatus.FORBIDDEN.value() == response.code()) {
            if (response.body().string().contains(String.valueOf(CommonConstants.EX_CLIENT_INVALID_CODE))) {
                log.info("Client Token Expire,Retry to request...");
                serviceAuthUtil.refreshClientToken();
                newRequest = chain.request()
                        .newBuilder()
                        .header(userAuthConfig.getTokenHeader(), BaseContextHandler.getToken())
                        .header(serviceAuthConfig.getTokenHeader(), serviceAuthUtil.getClientToken())
                        .header("ColaUserAgent", "web")
                        .build();
                response = chain.proceed(newRequest);
            }
        }
        return response;
    }
}
