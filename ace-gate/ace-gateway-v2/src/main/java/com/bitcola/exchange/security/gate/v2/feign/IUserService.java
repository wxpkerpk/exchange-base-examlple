package com.bitcola.exchange.security.gate.v2.feign;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.bitcola.exchange.security.gate.v2.fallback.UserServiceFallback;
import com.bitcola.exchange.security.api.vo.authority.PermissionInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;


/**
 * ${DESCRIPTION}
 *
 * @author wx
 * @create 2017-06-21 8:11
 */
@FeignClient(value = "ace-admin",fallback = UserServiceFallback.class)
public interface IUserService {
  @RequestMapping(value="/api/user/un/{username}/permissions",method = RequestMethod.GET)
  @Cached(name="getPermissionByUsername-", key="#username", expire = 120,cacheType = CacheType.LOCAL)
  List<PermissionInfo> getPermissionByUsername(@PathVariable("username") String username);


  @Cached(name="getAllPermissionInfo", expire = 60,cacheType = CacheType.LOCAL)
  @RequestMapping(value="/api/permissions",method = RequestMethod.GET)
  List<PermissionInfo> getAllPermissionInfo();
}
