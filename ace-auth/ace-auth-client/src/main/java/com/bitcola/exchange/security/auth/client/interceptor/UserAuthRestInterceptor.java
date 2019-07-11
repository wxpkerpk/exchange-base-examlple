package com.bitcola.exchange.security.auth.client.interceptor;

import com.bitcola.exchange.security.auth.client.annotation.IgnoreUserToken;
import com.bitcola.exchange.security.auth.client.config.UserAuthConfig;
import com.bitcola.exchange.security.auth.client.jwt.UserAuthUtil;
import com.bitcola.exchange.security.auth.common.util.jwt.IJWTInfo;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.constant.UserConstant;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.exception.auth.UserTokenException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by wx on 2017/9/10.
 */
public class UserAuthRestInterceptor extends HandlerInterceptorAdapter {
    private Logger logger = LoggerFactory.getLogger(UserAuthRestInterceptor.class);

    @Autowired
    private UserAuthUtil userAuthUtil;

    @Autowired
    private UserAuthConfig userAuthConfig;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 配置该注解，说明不进行用户拦截
        IgnoreUserToken annotation = handlerMethod.getBeanType().getAnnotation(IgnoreUserToken.class);
        if (annotation == null) {
            annotation = handlerMethod.getMethodAnnotation(IgnoreUserToken.class);
        }
        if (annotation != null) {
            return super.preHandle(request, response, handler);
        }
        String token = request.getHeader(userAuthConfig.getTokenHeader());
        if (StringUtils.isEmpty(token)) {
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if (cookie.getName().equals(userAuthConfig.getTokenHeader())) {
                        token = cookie.getValue();
                    }
                }
            }
        }
        IJWTInfo infoFromToken = userAuthUtil.getInfoFromToken(token);

        // zkq 2018/9/30 修改为去验证 token 在 redis 是否有效,保证非 web 端只能登陆一个账号
        String colaUserAgent = request.getHeader("ColaUserAgent");

        if (!"web".equals(colaUserAgent)){
            String uuid = redisTemplate.opsForValue().get(UserConstant.USER_LOGIN_KEY+infoFromToken.getId());
            if (StringUtils.isBlank(uuid)){
                throw new UserTokenException(ResponseCode.TOKEN_ERROR_MESSAGE);
            }
            String redisToken = redisTemplate.opsForValue().get(UserConstant.USER_LOGIN_TOKEN_KEY+infoFromToken.getId()+uuid);
            if (!token.equals(redisToken)){
                throw new UserTokenException(ResponseCode.TOKEN_ERROR_MESSAGE);
            }
        }

        BaseContextHandler.setUsername(infoFromToken.getUniqueName());
        BaseContextHandler.setName(infoFromToken.getName());
        BaseContextHandler.setUserID(infoFromToken.getId());
        BaseContextHandler.setToken(token);
        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContextHandler.remove();
        super.afterCompletion(request, response, handler, ex);
    }
}
