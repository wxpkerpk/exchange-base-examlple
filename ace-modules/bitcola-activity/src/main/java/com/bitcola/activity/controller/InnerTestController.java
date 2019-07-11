package com.bitcola.activity.controller;

import com.bitcola.activity.biz.SystemBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author zkq
 * @create 2018-11-30 11:16
 **/
@RestController
@RequestMapping("cola")
public class InnerTestController {

    @Autowired
    SystemBiz systemBiz;

}
