package com.bitcola.exchange.security.admin.biz;

import com.bitcola.exchange.security.admin.feign.IDataServiceFeign;
import com.bitcola.exchange.security.admin.mapper.ColaWithdrawMapper;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.AdminQuery;
import com.bitcola.exchange.security.common.util.MD5Utils;
import com.bitcola.me.entity.ColaMeBalanceWithdrawin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-12-18 15:47
 **/
@Service
public class ColaWithdrawBiz {

    @Autowired
    ColaWithdrawMapper mapper;

    @Autowired
    IDataServiceFeign dataServiceFeign;

    public TableResultResponse list(AdminQuery query) {
        List<ColaMeBalanceWithdrawin> list = mapper.list(query);
        Long total = mapper.total(query);
        return new TableResultResponse(total,list);
    }

    public Map<String, Object> info(String orderId) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> item = mapper.item(orderId);
        map.put("info",item);
        List<Map<String, Object>> inOut = mapper.inOut(item.get("user_id").toString());
        List<Map<String, Object>> deposit = new ArrayList<>();
        List<Map<String, Object>> withdraw = new ArrayList<>();
        for (Map<String, Object> m : inOut) {
            String type = m.get("type").toString();
            //String coinCode = m.get("coin_code").toString();
            //BigDecimal number = new BigDecimal(m.get("total").toString());
            if ("Deposit".equals(type)){
                deposit.add(m);
            } else {
                withdraw.add(m);
            }
        }
        Map<String, Object> assets = new HashMap<>();
        assets.put("deposit",deposit);
        assets.put("withdraw",withdraw);
        map.put("assets",assets);
        return map;
    }

    public int refuse(String orderId,String userId, String reason) {
        // 将冻结资金解冻
        BigDecimal number = mapper.withdrawNumber(orderId);
        String coinCode = mapper.withdrawCoinCode(orderId);
        boolean b = dataServiceFeign.transformBalance(userId, userId, coinCode, true, false, number, "", "");
        if (b){
            return mapper.withdrawRefuse(orderId,reason, EncoderUtil.WITHDRAW_KEY);
        } else {
            return 0;
        }
    }

    public void updateStatus(String orderId, String status, String reason) {
        mapper.updateStatus(orderId,status,reason, EncoderUtil.WITHDRAW_KEY);
    }

    public Map<String, String> orderWithdrawInfo(String orderId) {
        return mapper.orderWithdrawInfo(orderId);
    }

    public void withdrawSuccessByChain(String id, String hash, String userId, BigDecimal number, String coinCode) {
        // 修改订单状态为成功,
        mapper.updateWithdrawStatus(id,hash);
        // 修改用户资金
        mapper.updateWithdrawUserBalance(userId+coinCode,number,EncoderUtil.BALANCE_KEY);
    }
}
