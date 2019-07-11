package com.bitcola.chain.handle;

import com.bitcola.chain.annotation.RequestPath;
import com.bitcola.chain.util.PathUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

@Configuration
public class MessageRequestRegister implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        RequestPath requestBeanAnnotation = bean.getClass().getAnnotation(RequestPath.class);
        if (requestBeanAnnotation != null && !bean.getClass().isInterface()) {
            String parentPath = requestBeanAnnotation.value();
            Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
            for (Method method : methods) {
                RequestPath listener = AnnotationUtils.findAnnotation(method, RequestPath.class);
                if (listener != null){
                    String path = listener.value();
                    String currentPath = PathUtil.pathConcat(parentPath, path);
                    MessageRequestHandler.addHandler(currentPath, beanName, method);
                }
            }
        }
        return bean;
    }






}