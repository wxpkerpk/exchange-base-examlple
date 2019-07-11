package com.bitcola.activity.biz;

import com.bitcola.activity.entity.InnerTest;
import com.bitcola.activity.feign.IDataServiceFeign;
import com.bitcola.activity.mapper.InnerTestMapper;
import com.bitcola.exchange.security.common.constant.SystemBalanceConstant;
import com.bitcola.exchange.security.common.constant.UserConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author zkq
 * @create 2018-11-29 21:17
 **/
@Service
public class InnerTestBiz {

    @Autowired
    IDataServiceFeign dataServiceFeign;

    @Autowired
    InnerTestMapper mapper;

    //每晚5点发放
    //@Scheduled(cron = "0 0 5 * * ?")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void innerTest(Long startTime, Long endTime){
        Map<String,Integer> ids = new HashMap<>();
        //查询当用户有效下单(必须成交)
        BigDecimal total = mapper.total();
        if (total!=null && total.compareTo(new BigDecimal(3000000))>0){
            return;
        }
        List<Map<String,Object>> maps = mapper.innerTest(startTime,endTime); // 查询当天
        for (Map<String, Object> map : maps) {
            String user_id = map.get("user_id").toString();
            String orderId = map.get("id").toString();
            Integer integer = ids.get(user_id);
            if (integer == null){
                integer = 0;
            }
            if (integer<3){
                integer++;
                ids.put(user_id,integer);
                //判断当前交易是否已经发放奖励
                InnerTest test = new InnerTest();
                test.setUserId(user_id);
                test.setNumber(new BigDecimal(200));
                test.setOrderId(orderId);
                List<InnerTest> select = mapper.select(test);
                if (select.size()==0){
                    //加钱
                    dataServiceFeign.transformBalance(UserConstant.SYS_ACCOUNT_ID,user_id,"COLA",false,false,new BigDecimal(200), SystemBalanceConstant.REWARD_SYSTEM,"内测奖励");
                    // 记录日志
                    test.setId(UUID.randomUUID().toString());
                    test.setTimestamp(System.currentTimeMillis());
                    mapper.insertSelective(test);
                }
            }
        }
    }


    public static Long getStartTime(){
        long l = 24*60*60*1000; //1天
        long s = 3*60*60*1000;
        long currentTimeMillis = System.currentTimeMillis();
        return currentTimeMillis - currentTimeMillis%l - s;
    }
    public static Long getEndTime(){
        long l = 24*60*60*1000; //1天
        long s = 3*60*60*1000;
        long currentTimeMillis = System.currentTimeMillis();
        return currentTimeMillis - currentTimeMillis%l - s + l;
    }



}
