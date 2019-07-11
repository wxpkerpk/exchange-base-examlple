package com.bitcola.chain.proxy;

import com.bitcola.chain.annotation.Params;
import com.bitcola.chain.annotation.RequestPath;
import com.bitcola.chain.util.PathUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RegistryBean implements ApplicationContextAware, BeanDefinitionRegistryPostProcessor {
    private ApplicationContext ctx;

    List<Class> classList;

    public RegistryBean() {

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }




    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
         String scanScope= ctx.getEnvironment().getProperty("bitcola.chain.packageScan");
         if(scanScope==null) scanScope="/";
        ScanPackage scanPackage = new ScanPackage();
        try {
            this.classList = scanPackage.readPackageClazz(scanScope).stream().
                    filter(clazz -> clazz.getAnnotation(RequestPath.class) != null).
                    collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }


        for (Class clazz : this.classList) {
            Class<?> cls = clazz;
            String baseUrl = cls.getAnnotation(RequestPath.class).value();
            if (cls.isInterface()){
                // rpc
                var methods = cls.getMethods();
                for (var method : methods) {
                    String key = MessageRequestProxyFactory.getMethodKey(cls, method);
                    var params = method.getParameters();
                    String[] paramsName = new String[params.length];
                    for (int i = 0; i < params.length; i++) {
                        var paramAnnotation = params[i].getAnnotation(Params.class);
                        String paramName = paramAnnotation.value();
                        paramsName[i] = paramName;
                    }
                    String url = method.getAnnotation(RequestPath.class).value();
                    MessageRequestProxyFactory.paramsMap.put(key, paramsName);
                    MessageRequestProxyFactory.urlMap.put(key, PathUtil.pathConcat(baseUrl,url));
                }
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(cls);
                GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
                definition.getPropertyValues().add("interfaceClass", definition.getBeanClassName());
                definition.setBeanClass(MessageRequestProxyFactory.class);
                definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
                String name = lowerFirst(cls.getSimpleName());
                beanDefinitionRegistry.registerBeanDefinition(name, definition);
            }
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx=applicationContext;
    }

    private static String lowerFirst(String oldStr) {
        char[] chars = oldStr.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

}

