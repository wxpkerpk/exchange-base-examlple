package com.bitcola.exchange.security.admin.rest;

import com.bitcola.exchange.security.admin.biz.ColaWithdrawBiz;
import com.bitcola.exchange.security.admin.feign.IMeFeign;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.AdminQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 提现管理
 *
 * @author zkq
 * @create 2018-10-30 19:00
 **/
@RestController
@RequestMapping("cola/withdraw")
public class ColaWithdrawController {

    @Autowired
    ColaWithdrawBiz biz;

    @Autowired
    IMeFeign meFeign;

    @RequestMapping("list")
    public TableResultResponse list(@RequestParam Map<String, Object> params){
        AdminQuery query = new AdminQuery(params);
        return biz.list(query);
    }

    @RequestMapping("info")
    public AppResponse info(String orderId){
        Map<String,Object> map = biz.info(orderId);
        return AppResponse.ok().data(map);
    }

    @RequestMapping("audit")
    public AppResponse audit(String orderId,String userId,String status,String type,String reason){
        if (!"Withdraw".equals(type)){
            return AppResponse.error("只能审核提币");
        }
        if ("agree".equals(status)){
            String txId = meFeign.withdrawPass(orderId,userId);
            return AppResponse.ok().data(txId);
        }
        int i = biz.refuse(orderId,userId,reason);
        return AppResponse.ok().data("拒绝了 "+i+" 个");
    }

    @RequestMapping("updateStatus")
    public AppResponse updateStatus(String orderId,String status,String reason){
        if (!"Checking".equalsIgnoreCase(status)&&!"Exported".equalsIgnoreCase(status)
                &&!"Refuse".equalsIgnoreCase(status)&&!"Failed".equalsIgnoreCase(status)
                &&!"Completed".equalsIgnoreCase(status)){
            return AppResponse.error("状态参数没传对");
        }
        if (StringUtils.isBlank(reason)){
            return AppResponse.error("为啥改了这个参数");
        }
        Map<String,String> info = biz.orderWithdrawInfo(orderId);
        if (info.get("status").equalsIgnoreCase("Completed")){
            return AppResponse.error("已经完成的订单不允许修改状态");
        }
        if ("Refuse".equalsIgnoreCase(status)){
            int i = biz.refuse(orderId, info.get("user_id"),reason);
            return AppResponse.ok().data("拒绝了 "+i+" 个");
        }
        biz.updateStatus(orderId,status,reason);
        return AppResponse.ok();
    }


    @RequestMapping("withdrawSuccessByChain")
    public AppResponse withdrawSuccessByChain(String hash, String id, String userId, BigDecimal number,String coinCode){
        biz.withdrawSuccessByChain(id,hash,userId,number,coinCode);
        return AppResponse.ok();
    }


}
