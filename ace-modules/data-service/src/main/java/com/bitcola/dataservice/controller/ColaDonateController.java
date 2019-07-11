package com.bitcola.dataservice.controller;

import com.bitcola.dataservice.biz.ColaDonateBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 打赏
 *
 * @author zkq
 * @create 2018-10-31 15:07
 **/
@RestController
@RequestMapping("donate")
public class ColaDonateController {

    @Autowired
    ColaDonateBiz biz;

    @RequestMapping("donate")
    public boolean donate(String fromUser, String toUser, BigDecimal number) {
        try {
            biz.donate(fromUser, toUser, number);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @RequestMapping("donateNews")
    public boolean donateNews(String fromUser, String toUser, BigDecimal number, String coinCode) {
        try {
            biz.donateNews(fromUser, toUser, number ,coinCode);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
