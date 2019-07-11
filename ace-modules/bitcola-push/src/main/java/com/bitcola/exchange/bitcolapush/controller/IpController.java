package com.bitcola.exchange.bitcolapush.controller;

import com.bitcola.exchange.bitcolapush.http.IPUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zkq
 * @create 2018-11-13 18:52
 **/
@RequestMapping("ip")
@RestController
public class IpController {


    @RequestMapping("getAddress")
    public String getAddress(String ip){
        return IPUtil.getUserLocation(ip);
    }


}
