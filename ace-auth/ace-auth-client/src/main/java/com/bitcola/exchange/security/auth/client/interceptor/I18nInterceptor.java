package com.bitcola.exchange.security.auth.client.interceptor;

import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 国际化拦截器
 *
 * @author zkq
 * @create 2018-09-29 10:51
 **/
public class I18nInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String language = request.getHeader("ColaLanguage");
        if (StringUtils.isNotBlank(language) && !ColaLanguage.LANGUAGE_EN.equals(language)){
            //更改为国际化语种
            switch (language){
                case ColaLanguage.LANGUAGE_CN: ColaLanguage.setCN();break;

                default: ColaLanguage.setEN();break;
            }
        } else {
            ColaLanguage.setEN();
        }
        return super.preHandle(request, response, handler);
    }

}
