package com.bitcola.exchange.caculate.config;

import com.bitcola.exchange.security.auth.client.interceptor.ServiceAuthRestInterceptor;
import com.bitcola.exchange.security.auth.client.interceptor.UserAuthRestInterceptor;
import com.bitcola.exchange.security.common.handler.GlobalExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author wx
 * @date 2017/9/8
 */
@Configuration("meWebConfig")
@Primary
public class WebConfiguration implements WebMvcConfigurer {
    @Bean
    GlobalExceptionHandler getGlobalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(getServiceAuthRestInterceptor()).
                addPathPatterns(getIncludePathPatterns()).addPathPatterns("/api/user/validate");
        registry.addInterceptor(getUserAuthRestInterceptor()).
                addPathPatterns(getIncludePathPatterns());
    }

    @Bean
    ServiceAuthRestInterceptor getServiceAuthRestInterceptor() {
        return new ServiceAuthRestInterceptor();
    }

    @Bean
    UserAuthRestInterceptor getUserAuthRestInterceptor() {
        return new UserAuthRestInterceptor();
    }

    /**
     * 需要用户和服务认证判断的路径
     * @return
     */
    private ArrayList<String> getIncludePathPatterns() {
        ArrayList<String> list = new ArrayList<>();
        String[] urls = {
                "/getTransactionSign",
                "/makeOrder",
                "/cancelOrder",
                "/getSelfOrders",
                "/getOrders",
                "/orderManagement",
                "/orderHistory",
                "/balance/getBalance",
                "/favorites/list",
                "/homePageDataWithSignIn",
                "/homePageDataWithSignInAll",
                "/exchangelog/selectByUser"
        };
        Collections.addAll(list, urls);
        return list;
    }

}
