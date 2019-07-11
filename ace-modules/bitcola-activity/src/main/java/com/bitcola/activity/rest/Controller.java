package com.bitcola.activity.rest;

import com.bitcola.activity.biz.InnerTestBiz;
import com.bitcola.activity.biz.SignUpBiz;
import com.bitcola.activity.biz.SystemBiz;
import com.bitcola.activity.feign.IDataServiceFeign;
import com.bitcola.exchange.security.common.constant.UserConstant;
import com.bitcola.me.entity.ColaUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zkq
 * @create 2018-11-29 20:47
 **/
@RestController
@RequestMapping("activity")
public class Controller {

    @Autowired
    SystemBiz systemBiz;

    @Autowired
    SignUpBiz signUpBiz;
    @Autowired
    InnerTestBiz innerTestBiz;

    @Autowired
    IDataServiceFeign dataServiceFeign;











}
