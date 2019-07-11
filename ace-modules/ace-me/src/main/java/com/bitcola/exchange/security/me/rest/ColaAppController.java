package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.me.biz.ColaAppBannerBiz;
import com.bitcola.me.entity.ColaAppBanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zkq
 * @create 2019-03-20 11:04
 **/
@RestController
@RequestMapping("app")
public class ColaAppController {

    @Autowired
    ColaAppBannerBiz bannerBiz;

    @RequestMapping("banner")
    public AppResponse banner(){
        List<ColaAppBanner> list = bannerBiz.banner();
        return AppResponse.ok().data(list);
    }

}
