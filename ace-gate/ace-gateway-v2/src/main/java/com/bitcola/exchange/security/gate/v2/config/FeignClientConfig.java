package com.bitcola.exchange.security.gate.v2.config;

import com.bitcola.exchange.security.gate.v2.feign.ISystemMaintainFeign;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import feign.Contract;
import feign.Feign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kangxuan
 * @date 2019/1/16 0016 11:13.
 * @Description: 自定义 feign实例 配置
 */
@Configuration
public class FeignClientConfig {
	/**
	* FeignClientFactoryBean 该工厂类中 设置builder属性时就是通过该对象，源码中可看到
	*/
    @Autowired
    private FeignContext feignContext;

	/**
	* 通过注入Eureka实例对象，就不用手动指定url，只需要指定服务名即可
	*/
    @Autowired
    private EurekaClient eurekaClient;


    private <T> T create(Class<T> clazz,String serverId){
        InstanceInfo nextServerFromEureka = eurekaClient.getNextServerFromEureka(serverId,false);
        return Feign.builder()
                .encoder(feignContext.getInstance(serverId,feign.codec.Encoder.class))
                .decoder(feignContext.getInstance(serverId,feign.codec.Decoder.class))
                .contract(feignContext.getInstance(serverId, Contract.class))
                .target(clazz, nextServerFromEureka.getHomePageUrl());

    }

	  @Bean
	   public ISystemMaintainFeign getISystemMaintainFeign(){
	       return create(ISystemMaintainFeign.class,"ace-auth");
	   }
}