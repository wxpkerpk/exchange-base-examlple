package com.bitcola.activity.biz;

import com.bitcola.activity.entity.Exchange;
import com.bitcola.activity.feign.IDataServiceFeign;
import com.bitcola.activity.mapper.ExchangeRewardMapper;
import com.bitcola.exchange.security.common.constant.SystemBalanceConstant;
import com.bitcola.exchange.security.common.constant.UserConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author zkq
 * @create 2018-12-26 20:04
 **/
@Service
public class ExchangeRewardBiz {

    @Autowired
    ExchangeRewardMapper exchangeRewardMapper;

    @Autowired
    IDataServiceFeign dataServiceFeign;
    private static BigDecimal ALL = new BigDecimal("100000000");
    private static BigDecimal ER = new BigDecimal("25000000");
    private static BigDecimal WU = new BigDecimal("50000000");
    private static BigDecimal QI = new BigDecimal("75000000");

    public void exchange(){
        // 查询当前这个小时的交易量
        List<Map<String, Object>> select = exchangeRewardMapper.selectItem(getStartTime(),getEndTime());
        BigDecimal number = exchangeRewardMapper.total();
        if (number == null) number = BigDecimal.ZERO;
        BigDecimal rate = new BigDecimal(5);
        if (ALL.compareTo(number)>0){
            if (number.compareTo(QI)>0){
                rate = new BigDecimal(20);
            } else if (number.compareTo(WU)>0){
                rate = new BigDecimal(15);
            } else if (number.compareTo(ER)>0){
                rate = new BigDecimal(10);
            }
            Map<String,BigDecimal> reward = new HashMap<>();
            for (Map<String, Object> map : select) {
                String to_count = map.get("to_count").toString();
                String user_id = map.get("user_id").toString();
                String to_user_id = map.get("to_user_id").toString();
                BigDecimal divide = new BigDecimal(to_count).divide(rate, 2, RoundingMode.DOWN);
                if (divide.compareTo(BigDecimal.ZERO)>0){
                    //reward(divide,user_id,orderId);
                    //reward(divide,to_user_id,orderId);
                    BigDecimal from = reward.get(user_id);
                    if (from == null){
                        from = BigDecimal.ZERO;
                    }
                    reward.put(user_id,from.add(divide));
                    BigDecimal to = reward.get(to_user_id);
                    if (to == null){
                        to = BigDecimal.ZERO;
                    }
                    reward.put(to_user_id,to.add(divide));
                }
            }
            for (String userId : reward.keySet()) {
                reward(reward.get(userId),userId,"交易送币");
            }
        }
    }


    private void reward(BigDecimal number,String userId, String orderId){
        dataServiceFeign.transformBalance(UserConstant.SYS_ACCOUNT_ID,userId,"COLA",false,false,number, SystemBalanceConstant.REWARD_SYSTEM,"交易送币");
        // 记录日志
        Exchange exchange = new Exchange();
        exchange.setId(UUID.randomUUID().toString());
        exchange.setTimestamp(System.currentTimeMillis());
        exchange.setCoinCode("COLA");
        exchange.setNumber(number);
        exchange.setUserId(userId);
        exchange.setDescription(orderId);
        try {
            exchangeRewardMapper.insertSelective(exchange);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static Long getStartTime(){
        long l = 60*60*1000; //1小时
        long currentTimeMillis = System.currentTimeMillis();
        return currentTimeMillis - currentTimeMillis%l - l;
    }
    public static Long getEndTime(){
        long l = 60*60*1000; //1天
        long currentTimeMillis = System.currentTimeMillis();
        return currentTimeMillis - currentTimeMillis%l;
    }



}
