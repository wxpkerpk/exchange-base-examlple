package com.bitcola.chain.handle;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bitcola.chain.annotation.Params;
import com.bitcola.chain.config.SpringContextsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class MessageRequestHandler {


    @Autowired
    StringRedisTemplate redisTemplate;


    @Autowired
    SpringContextsUtil springContextsUtil;




    static ConcurrentMap<String, MethodHandler> routeMapper = new ConcurrentHashMap<>();

    public static void addHandler(String path, String beanName, Method handlerMethod) {
        MethodHandler methodHandler = new MethodHandler();
        methodHandler.setBeanName(beanName);
        methodHandler.setHandlerMethod(handlerMethod);
        routeMapper.put(path, methodHandler);
    }

    public Object handleRequest(String path, Map<String, Object> params) throws Throwable {
        var handler = routeMapper.getOrDefault(path, null);
        if (handler != null) {
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(params));
            var beanName = handler.getBeanName();
            var bean = springContextsUtil.getBean(beanName);
            var method = handler.getHandlerMethod();
            var paramList = method.getParameters();
            var paramsTypes = method.getParameterTypes();
            Object paramObject[] = new Object[paramList.length];
            int index = 0;
            for (Parameter p : paramList) {
                Object perParam=null;
                String paramName="";
                Params paramAnnotation=p.getAnnotation(Params.class);
                if(paramAnnotation!=null) paramName=paramAnnotation.value();
                if(jsonObject.containsKey(paramName)) {
                    perParam = jsonObject.getObject(paramName, paramsTypes[index]);
                }
                paramObject[index] = perParam;
                index++;
            }
            return ReflectionUtils.invokeMethod(method, bean, paramObject);
        }
        throw new Exception("404 request: "+path);

    }

    public static class MethodHandler {
        public Method getHandlerMethod() {
            return handlerMethod;
        }
        public void setHandlerMethod(Method handlerMethod) {
            this.handlerMethod = handlerMethod;
        }
        public String getBeanName() {
            return beanName;
        }
        public void setBeanName(String beanName) {
            this.beanName = beanName;
        }
        String beanName;
        Method handlerMethod;
    }

}
