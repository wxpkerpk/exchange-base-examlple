package com.bitcola.exchange.security.me.controller;

import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.me.biz.ColaCoinBiz;
import com.bitcola.exchange.security.me.biz.ColaMeBalanceWithdrawinBiz;
import com.bitcola.exchange.security.me.biz.ColaUserBiz;
import com.bitcola.exchange.security.me.vo.InWithdrawDetail;
import com.bitcola.me.entity.ColaCoin;
import com.bitcola.me.entity.ColaUserEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zkq
 * @create 2018-12-24 14:39
 **/
@RequestMapping("admin")
@RestController
public class AdminController {

    @Autowired
    ColaMeBalanceWithdrawinBiz withdrawinBiz;

    @Autowired
    ColaUserBiz userBiz;

    @Autowired
    ColaCoinBiz colaCoinBiz;

    @RequestMapping("withdrawPass")
    public String withdrawPass(String orderId,String userId){
        String userID = BaseContextHandler.getUserID();
        if (StringUtils.isNotBlank(userID)){
            Integer id = Integer.valueOf(userID);
            if (id>100000){
                return null;
            }
        } else {
            System.out.println("非法调用");
            return null;
        }
        InWithdrawDetail detail = withdrawinBiz.detail(orderId);
        if (detail.getStatus().equals("Completed")){
            System.out.println("当前订单已经完成");
            return null;
        }
        return withdrawinBiz.withdraw(orderId,detail.getCoinCode());
    }


}
