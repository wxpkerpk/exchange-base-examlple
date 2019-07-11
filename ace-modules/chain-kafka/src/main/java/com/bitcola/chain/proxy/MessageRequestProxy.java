package com.bitcola.chain.proxy;


import com.alibaba.fastjson.JSONObject;
import com.bitcola.chain.config.SpringContextsUtil;
import com.bitcola.chain.entity.BitColaChainMessage;
import com.bitcola.chain.kafka.producer.PublisherAdapter;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Proxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class MessageRequestProxy implements InvocationHandler {
    public static Map<String, Object> locks = new ConcurrentHashMap<>();
    public static Map<String, BitColaChainMessage> messages = new ConcurrentHashMap<>();

    private Class<?> interfaceClass;

    public Object bind(Class<?> cls) {
        this.interfaceClass = cls;
        return Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{interfaceClass}, this);
    }


    PublisherAdapter publish;

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Exception {
        if (publish == null) {
            publish = SpringContextsUtil.applicationContext.getBean(PublisherAdapter.class);
        }
        String key=MessageRequestProxyFactory.getMethodKey(interfaceClass,method);
        //请求的url
        var url= MessageRequestProxyFactory.urlMap.get(key);
        //请求的参数名称
        var paramsNames=MessageRequestProxyFactory.paramsMap.get(key);
        //执行发送操作并且阻塞 得到返回的消息,返回结果
        String id = UUID.randomUUID().toString();
        boolean isModuleRequest = false;
        Map<String,Object> params = new HashMap<>();
        for (int i = 0; i < paramsNames.length; i++) {
            String paramsName = paramsNames[i];
            Object param = objects[i];
            params.put(paramsName,param);
            if (paramsName.equalsIgnoreCase("module")){
                isModuleRequest = true;
            }
        }
        Object lock = new Object();
        locks.put(id,lock);
        synchronized (lock) {
            if (isModuleRequest){
                publish.sendToModule(params,params.get("module").toString(),url,id);
            } else {
                publish.sendNormal(params,url,id);
            }
            lock.wait(60000);
        }
        BitColaChainMessage response = messages.get(id);
        if (response == null){
            throw new TimeoutException("请求超时:"+url);
        }
        messages.remove(id);
        if (response.isSuccess()){
            Class<?> returnType = method.getReturnType();
            return JSONObject.parseObject(JSONObject.toJSONString(response.getData()),returnType);
        }
        throw new Exception(response.getErrorMsg());
    }



}


