package com.bitcola.exchange.security.gate.v2.service;

import com.bitcola.exchange.security.api.vo.authority.PermissionInfo;
import com.bitcola.exchange.security.gate.v2.feign.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/*
 * @author:wx
 * @description:用户权限获取
 * @create:2018-07-14  23:01
 */
@Service
public class UserService {


    @Autowired
    private IUserService iUserService;
//    @Cache(key = "getPermissionByUsername:{1}",expire = 1)
    public List<PermissionInfo> getPermissionByUsername(String username){
        return iUserService.getPermissionByUsername(username);

    }
//    @Cache(key = "getAllPermissionInfo",expire = 1)
    public List<PermissionInfo> getAllPermissionInfo(){
        return iUserService.getAllPermissionInfo();


    }

}
