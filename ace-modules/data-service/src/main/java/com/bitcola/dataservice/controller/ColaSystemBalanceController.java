package com.bitcola.dataservice.controller;

import com.bitcola.dataservice.biz.ColaSystemBalanceBiz;
import com.bitcola.me.entity.ColaSystemBalance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 系统资金
 * @author zkq
 * @create 2018-10-25 15:50
 **/
@RestController
@RequestMapping("systemBalance")
public class ColaSystemBalanceController {

    @Autowired
    ColaSystemBalanceBiz biz;

    /**
     * 所有字段不允许为空
     * @param userId 扣除的人
     * @param amount 数量
     * @param coinCode 币种
     * @param type   交易类型
     * @param description 描述
     * @return
     */
    @RequestMapping("in")
    public boolean in(String userId, BigDecimal amount,String coinCode,String type,String description){
        return biz.in(userId,amount,coinCode,type,description);
    }



    /**
     * 所有字段不允许为空
     * @param userId 给哪个发钱
     * @param amount 数量
     * @param coinCode 币种
     * @param type  交易类型
     * @param description 描述
     * @return
     */
    @RequestMapping("out")
    public boolean out(String userId, BigDecimal amount,String coinCode,String type,String description){
        return biz.out(userId,amount,coinCode,type,description);
    }


    @RequestMapping("transformBalance")
    public boolean transformBalance(String fromUser,String toUser,String coinCode,boolean fromFrozen,boolean toFrozen,
                                    BigDecimal number,String type,String description){
        return biz.transformBalance(fromUser,toUser,coinCode,fromFrozen,toFrozen,number,type,description);
    }



}
