package com.bitcola.exchange.security.gate.v2.service;

import com.bitcola.exchange.security.gate.v2.feign.ISystemMaintainFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统维护
 *
 * @author zkq
 * @create 2018-11-20 11:36
 **/
@Service
public class MaintainService {

    public static Map<String,Map<String,Object>> modules = new HashMap<>();

    @Autowired
    ISystemMaintainFeign maintainFeign;

    //@Scheduled(cron = "0 */5 * * * ?")
    //public void load(){
    //    try {
    //        List<Map<String,Object>> list = maintainFeign.load();
    //        for (Map<String, Object> map : list) {
    //            String module = map.get("module").toString();
    //            modules.put(module,map);
    //        }
    //    } catch (Exception e){
    //
    //    }
    //}



}
