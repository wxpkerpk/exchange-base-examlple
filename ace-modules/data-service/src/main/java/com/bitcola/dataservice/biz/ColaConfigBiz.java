package com.bitcola.dataservice.biz;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.dataservice.mapper.ColaConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 配置
 *
 * @author zkq
 * @create 2018-10-18 15:13
 **/
@Service
public class ColaConfigBiz {

    @Autowired
    ColaConfigMapper configMapper;


    @Cached(key = "#config",expire = 60,cacheType = CacheType.LOCAL)
    public String getConfig(String config) {
        return configMapper.getConfig(config);
    }
}
