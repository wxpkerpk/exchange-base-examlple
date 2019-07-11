package com.bitcola.exchange.launchpad.service;

import com.bitcola.exchange.launchpad.config.DelayQueueBySpeedMap;
import com.bitcola.exchange.launchpad.constant.ExchangeLogStatus;
import com.bitcola.exchange.launchpad.dto.ColaUserBalance;
import com.bitcola.exchange.launchpad.entity.ColaLaunchpadExchangeLog;
import com.bitcola.exchange.launchpad.mapper.ColaLaunchpadProjectIeoMapper;
import com.bitcola.exchange.launchpad.message.ClearMessage;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.constant.UserConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zkq
 * @create 2019-03-14 17:43
 **/
@Service
public class ClearService implements ApplicationRunner {

    public static final Map<String,BigDecimal> REMAIN = new ConcurrentHashMap<>();

    @Autowired
    DelayQueueBySpeedMap<ClearMessage> queueMap;

    @Autowired
    ClearService service;

    @Autowired
    ColaLaunchpadProjectIeoMapper mapper;


    public void start (String id,BigDecimal remain,Long end){
        REMAIN.put(id,remain);
        new Thread(() -> {
            while (REMAIN.get(id).compareTo(BigDecimal.ZERO)>0 && System.currentTimeMillis()<end){
                try {
                    List<ClearMessage> message = queueMap.getMessage(id);
                    if (message.size() > 0){
                        service.dealMessage(message,id);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Transactional
    public void dealMessage(List<ClearMessage> messages,String id) {
        List<ColaLaunchpadExchangeLog> exchangeLogs = new ArrayList<>();
        Map<String, ColaUserBalance> userBalanceMap = new HashMap<>();
        BigDecimal sellNumber = BigDecimal.ZERO;
        for (ClearMessage message : messages) {
            exchangeLogs.add(createExchangeLog(message.getId(),message.getUserId(),message.getCoinCode(),
                    message.getSymbol(),message.getPrice(),message.getBuyRealNumber(),message.getTimestamp(),message.getReward()));
            sellNumber = sellNumber.add(message.getBuyRealNumber()).add(message.getReward());
            // 项目方扣除冻结数量
            ColaUserBalance projectBalance = userBalanceMap.computeIfAbsent(message.getProjectUserId() + message.getCoinCode(),
                    k -> new ColaUserBalance(message.getProjectUserId() + message.getCoinCode()));
            projectBalance.setFrozen(projectBalance.getFrozen().subtract(sellNumber));
            // 用户扣除 number * price 的冻结 , 返还 ( number-realNumber ) * price 的可用
            ColaUserBalance userBalance = userBalanceMap.computeIfAbsent(message.getUserId() + message.getSymbol(),
                    k -> new ColaUserBalance(message.getUserId() + message.getSymbol()));
            userBalance.setFrozen(userBalance.getFrozen().subtract(message.getNumber().multiply(message.getPrice())));
            BigDecimal back = (message.getNumber().subtract(message.getBuyRealNumber())).multiply(message.getPrice());
            userBalance.setAvailable(userBalance.getAvailable().add(back));

            // 扣除的资金加到 1 账户上
            ColaUserBalance adminBalance = userBalanceMap.computeIfAbsent(UserConstant.SYS_ADMIN + message.getCoinCode(),
                    k -> new ColaUserBalance(UserConstant.SYS_ADMIN + message.getCoinCode()));
            adminBalance.setFrozen(adminBalance.getAvailable().add(sellNumber));
            ColaUserBalance adminBalanceSymbol = userBalanceMap.computeIfAbsent(UserConstant.SYS_ADMIN + message.getSymbol(),
                    k -> new ColaUserBalance(UserConstant.SYS_ADMIN + message.getSymbol()));
            adminBalanceSymbol.setFrozen(adminBalance.getAvailable().add(message.getBuyRealNumber().multiply(message.getPrice())));
        }
        List<ColaUserBalance> balanceList = new ArrayList<>();
        for (String key : userBalanceMap.keySet()) {
            balanceList.add(userBalanceMap.get(key));
        }

        // 批量插入,修改用户余额
        if (balanceList.size()>0){
            List<String> ids = mapper.selectBatch(balanceList);
        }
        if (exchangeLogs.size()>0){
            mapper.batchInsertExchangeLog(exchangeLogs);
        }
        Integer status = 1;
        if (REMAIN.get(id).compareTo(BigDecimal.ZERO)==0){
            status = 2;
        }
        mapper.updateProjectRemainAndStatus(id,sellNumber,status);
        mapper.batchUpdateUserBalance(balanceList, EncoderUtil.BALANCE_KEY);
    }

    private ColaLaunchpadExchangeLog createExchangeLog(String projectId,String userId,String coinCode,String symbol,BigDecimal price,
                                                       BigDecimal number,Long timestamp,BigDecimal reward){
        ColaLaunchpadExchangeLog log = new ColaLaunchpadExchangeLog();
        log.setId(UUID.randomUUID().toString());
        log.setProjectId(projectId);
        log.setUserId(userId);
        log.setCoinCode(coinCode);
        log.setSymbol(symbol);
        log.setPrice(price);
        log.setNumber(number);
        log.setTimestamp(timestamp);
        log.setStatus(ExchangeLogStatus.NOT_ISSUED);
        log.setReward(reward);
        return log;
    }


    @Override
    public void run(ApplicationArguments args) {
        //List<Map<String,Object>> maps = mapper.selectIeoProject();
        //for (Map<String, Object> map : maps) {
        //    String id = map.get("id").toString();
        //    Long end = Long.valueOf(map.get("end").toString());
        //    BigDecimal remain = new BigDecimal(map.get("remain").toString());
        //    this.start(id,remain,end);
        //}
    }
}
