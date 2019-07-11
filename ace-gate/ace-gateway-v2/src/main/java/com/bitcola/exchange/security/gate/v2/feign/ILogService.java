package com.bitcola.exchange.security.gate.v2.feign;

import com.bitcola.exchange.security.api.vo.log.LogInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * ${DESCRIPTION}
 *
 * @author wx
 * @create 2017-07-01 15:16
 */
@FeignClient("ACE-ADMIN")
public interface ILogService {
  @RequestMapping(value="/api/log/save",method = RequestMethod.POST)
  public void saveLog(LogInfo info);
}
