package com.bitcola.exchange.security.admin.rpc;

import com.bitcola.exchange.security.admin.biz.GateLogBiz;
import com.bitcola.exchange.security.admin.entity.GateLog;
import com.bitcola.exchange.security.api.vo.log.LogInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ${DESCRIPTION}
 *
 * @author wx
 * @create 2017-07-01 14:39
 */
@RequestMapping("api")
@RestController
public class LogRest {
    @Autowired
    private GateLogBiz gateLogBiz;
    @RequestMapping(value="/log/save",method = RequestMethod.POST)
    public @ResponseBody void saveLog(@RequestBody LogInfo info){
        GateLog log = new GateLog();
        BeanUtils.copyProperties(info,log);
        gateLogBiz.insertSelective(log);
    }
}
