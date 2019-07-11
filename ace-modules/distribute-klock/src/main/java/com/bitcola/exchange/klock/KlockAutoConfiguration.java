package com.bitcola.exchange.klock;

import com.bitcola.exchange.klock.core.BusinessKeyProvider;
import io.netty.channel.nio.NioEventLoopGroup;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import com.bitcola.exchange.klock.config.KlockConfig;
import com.bitcola.exchange.klock.core.KlockAspectHandler;
import com.bitcola.exchange.klock.core.LockInfoProvider;
import com.bitcola.exchange.klock.lock.LockFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.ClassUtils;

/**
 * Created by kl on 2017/12/29.
 * Content :klock自动装配
 */
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(KlockConfig.class)
@Import({KlockAspectHandler.class})
public class KlockAutoConfiguration {

    @Autowired
    private KlockConfig klockConfig;

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    RedissonClient redisson() throws Exception {
        Config config = new Config();
        if(klockConfig.getClusterServer()!=null){
            config.useClusterServers().setPassword(klockConfig.getPassword())
                    .addNodeAddress(klockConfig.getClusterServer().getNodeAddresses());
        }else {
            config.useSingleServer().setAddress(klockConfig.getHost())
                    .setDatabase(klockConfig.getDatabase())
                    .setPassword(klockConfig.getPassword());
        }
        Codec codec=(Codec) ClassUtils.forName(klockConfig.getCodec(),ClassUtils.getDefaultClassLoader()).newInstance();
        config.setCodec(codec);
        config.setEventLoopGroup(new NioEventLoopGroup());
        return Redisson.create(config);
    }

    @Bean
    public LockInfoProvider lockInfoProvider(){
        return new LockInfoProvider();
    }

    @Bean
    public BusinessKeyProvider businessKeyProvider(){
        return new BusinessKeyProvider();
    }

    @Bean
    public LockFactory lockFactory(){
        return new LockFactory();
    }
}
