package com.bitcola.chain.mapper;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.chain.entity.ColaSmsEarlyWarning;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 区块链短信预警
 */
@Repository
public interface ColaSmsEarlyWarningMapper {

    @Cached(expire = 1,timeUnit = TimeUnit.HOURS,cacheType = CacheType.LOCAL)
    List<ColaSmsEarlyWarning> getWarningContract();


    List<String> getWarningTelephoneByGroup(@Param("group") String smsGroup);
}
