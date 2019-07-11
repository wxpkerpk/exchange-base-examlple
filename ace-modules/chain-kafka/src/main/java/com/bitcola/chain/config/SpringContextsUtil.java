package com.bitcola.chain.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextsUtil implements ApplicationContextAware {

    public static ApplicationContext applicationContext;

    public Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }
    public static Object build(String beanName) {
        return applicationContext.getBean(beanName);
    }

    public <T> T getBean(String beanName, Class<T> clazs) {
        return clazs.cast(getBean(beanName));
    }
    public static <T> T build(String beanName, Class<T> clazs) {
        return applicationContext.getBean(beanName,clazs);
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        SpringContextsUtil.applicationContext = applicationContext;
    }
}