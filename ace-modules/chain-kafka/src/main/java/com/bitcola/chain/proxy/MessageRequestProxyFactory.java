package com.bitcola.chain.proxy;

import feign.InvocationHandlerFactory;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MessageRequestProxyFactory<T> implements FactoryBean<T> {
    private Class<T> interfaceClass;
    public static Map<String,String[]> paramsMap=new HashMap<>(64);
    public static Map<String,String>urlMap=new HashMap<>(32);

    public Class<T> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    @Override
    public T getObject() throws Exception {
        return (T) new MessageRequestProxy().bind(interfaceClass);
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
    public static String getMethodKey(Class clazz, Method method) {
        return clazz.getName() + "_" + method.getName();
    }
}

