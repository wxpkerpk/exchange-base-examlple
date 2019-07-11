package com.bitcola.exchange.service;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.caculate.entity.RewardLog;
import com.bitcola.exchange.constant.*;
import com.bitcola.exchange.entity.BatchBalance;
import com.bitcola.exchange.entity.MatchRecord;
import com.bitcola.exchange.mapper.*;
import com.bitcola.exchange.message.MatchMessage;
import com.bitcola.exchange.message.MatchRecordMessage;
import com.bitcola.exchange.message.OrderMessage;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.constant.SystemBalanceConstant;
import com.bitcola.exchange.security.common.constant.UserConstant;
import com.bitcola.exchange.websocket.OrderNotifyEntity;
import com.bitcola.exchange.websocket.OrderNotifyMessage;
import com.bitcola.exchange.websocket.PersonOrderNotifyMessage;
import com.bitcola.me.entity.ColaSystemBalance;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


/**
 * @author zkq
 * @create 2019-02-18 14:33
 **/
@Service
@Log4j2
public class ClearingHandlerService {

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    MatchRecordMapper matchRecordMapper;

    @Autowired
    AccountService accountService;

    @Autowired
    BalanceMapper balanceMapper;

    @Autowired
    ColaSystemBalanceMapper systemBalanceMapper;
    @Autowired
    ClearingHandlerService clearingHandlerService;

    public static final Map<String,String> inviterMap = new HashMap<>();


    // ===================   批量操作  ========================




    public Map<String, Object> batchProcessMatchMessage(List<MatchMessage> message,String pair) {
        Map<String, Object> notifyMap = new HashMap<>();
        List<MatchRecord> logList = new ArrayList<>(); // 日志记录
        Map<String, OrderMessage> orderMap = new LinkedHashMap<>(); // 订单
        Map<String,BatchBalance> balanceMap = new LinkedHashMap<>(); // 余额更新
        List<RewardLog> rewardLog = new ArrayList<>(); // 邀请日志
        List<ColaSystemBalance> feeLog = new ArrayList<>(); // 手续费日志

        List<String> userIds = new ArrayList<>();
        List<String> userIdsNoCache = new ArrayList<>();
        for (MatchMessage msg : message) {
            orderMap.putAll(msg.getOrderMap());
        }

        for (String orderId : orderMap.keySet()) {
            OrderMessage orderMessage = orderMap.get(orderId);
            String userId = orderMessage.getUserId();
            userIds.add(userId);
            if (!inviterMap.containsKey(userId)){
                userIdsNoCache.add(userId);
            }
        }
        BigDecimal inviterRate = accountService.getTransactionInviterRate();
        if (userIdsNoCache.size() > 0){
            List<Map<String,String>> inviterList = balanceMapper.getInviterUserIdList(userIdsNoCache);
            for (Map<String, String> map : inviterList) {
                inviterMap.put(map.get("u"),map.get("inviter"));
            }
        }

        for (MatchMessage msg : message) {
            if (MatchMessageType.MATCH_RESULT.equals(msg.getType())) {
                processMatched(msg,orderMap,balanceMap,logList,inviterRate,rewardLog,feeLog);
            } else if (MatchMessageType.MATCH_CANCEL.equals(msg.getType())) {
                processCanceled(msg,orderMap,balanceMap);
            } else {
                log.info("未实现的订单类型");
            }
        }

        // 批量数据库操作
        List<OrderMessage> orderList = new ArrayList<>();
        for (String key : orderMap.keySet()) {
            orderList.add(orderMap.get(key));
        }
        List<BatchBalance> balanceList = new ArrayList<>();
        for (String key : balanceMap.keySet()) {
            balanceList.add(balanceMap.get(key));
        }
        balanceList.stream().sorted(Comparator.comparing(BatchBalance::getId));

        // 订单推送
        OrderNotifyMessage orderNotifyMessage = new OrderNotifyMessage();
        for (MatchRecord record : logList) {
            if (record.getType().equals(MatchRecordType.TAKER)){
                orderNotifyMessage.getList().add(new OrderNotifyEntity(record.getPrice(),record.getNumber(),record.getDirection(),record.getTimestamp()));
            }
        }
        notifyMap.put(NotifyMessageType.ORDER,orderNotifyMessage);
        notifyMap.put(NotifyMessageType.PERSON_ORDER,new PersonOrderNotifyMessage(pair,userIds));
        return clearingHandlerService.modifyData(notifyMap, logList, rewardLog, feeLog, orderList, balanceList);
    }
    @Transactional
    public Map<String, Object> modifyData(Map<String, Object> notifyMap, List<MatchRecord> logList, List<RewardLog> rewardLog, List<ColaSystemBalance> feeLog, List<OrderMessage> orderList, List<BatchBalance> balanceList) {
        List<Map<String, Object>> balance= balanceMapper.selectBatch(balanceList);
        Map<String,Map<String, Object>> balanceMap = new HashMap<>();
        for (Map<String, Object> map : balance) {
            balanceMap.put(map.get("id").toString(),map);
        }
        //  对用户的之前的余额做一个 json 存盘
        for (MatchRecord record : logList) {
            Map<String, Object> coinCode = balanceMap.get(record.getUserId() + record.coinCode());
            Map<String, Object> symbol = balanceMap.get(record.getUserId() + record.symbol());
            String dump = JSONObject.toJSONString(new Map[]{coinCode, symbol});
            record.setDump(dump);
        }

        if (logList.size()>0){
            int index = 0;
            int end = 0;
            do {
                end += 500;
                matchRecordMapper.batchInsert(logList.subList(index,end > logList.size() ? logList.size() : end ));
                index = end;
            } while (end < logList.size());
        }
        if (feeLog.size()>0){
            int index = 0;
            int end = 0;
            do {
                end += 500;
                systemBalanceMapper.batchInsert(feeLog.subList(index,end > feeLog.size() ? feeLog.size() : end ));
                index = end;
            } while (end < feeLog.size());
        }
        if (rewardLog.size()>0){
            int index = 0;
            int end = 0;
            do {
                end += 500;
                systemBalanceMapper.batchInsertReward(rewardLog.subList(index,end > rewardLog.size() ? rewardLog.size() : end ));
                index = end;
            } while (end < rewardLog.size());
        }
        if (orderList.size()>0){
            orderMapper.batchUpdate(orderList); // 更新剩余金额和订单状态,成交均价
        }
        if (balanceList.size()>0){
            balanceMapper.batchUpdate(balanceList, EncoderUtil.BALANCE_KEY);
        }

        return notifyMap;
    }


    /**
     * 开始处理
     * @param matchResult
     * @param orderMap
     * @param balanceMap
     * @param logList
     * @param inviterRate
     * @param rewardLog
     * @param feeLog
     */
    private void processMatched(MatchMessage matchResult, Map<String,OrderMessage> orderMap, Map<String,BatchBalance> balanceMap, List<MatchRecord> logList,BigDecimal inviterRate,List<RewardLog> rewardLog,List<ColaSystemBalance> feeLog) {
        BigDecimal totalSpent = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        final long timestamp = matchResult.getTimestamp();
        OrderMessage takerOrder = null;
        for (MatchRecordMessage record : matchResult.getMatchRecords()) {
            BigDecimal price = record.getMatchPrice();
            BigDecimal number = record.getMatchNumber();
            if (takerOrder == null) {
                takerOrder = orderMap.get(record.getTakerOrderId());
            }
            OrderMessage makerOrder = orderMap.get(record.getMakerOrderId());
            clearMakerOrder(takerOrder, makerOrder, price, number, record.getMakerStatus(), timestamp, balanceMap,logList,inviterRate,rewardLog,feeLog);
            totalSpent = totalSpent.add(price.multiply(number));
            totalAmount = totalAmount.add(number);
        }
        //
        clearTakerOrder(takerOrder, matchResult.getTakerStatus(), totalSpent, totalAmount,balanceMap,inviterRate,rewardLog,feeLog,matchResult.getTakerRemain());
    }

    /**
     * 挂单处理
     * @param takerOrder
     * @param makerOrder
     * @param price
     * @param number
     * @param makerStatus
     * @param timestamp
     * @param balanceMap
     * @param logList
     * @param inviterRate
     * @param rewardLog
     * @param feeLog
     */
    private void clearMakerOrder(OrderMessage takerOrder, OrderMessage makerOrder, BigDecimal price, BigDecimal number, String makerStatus, long timestamp, Map<String, BatchBalance> balanceMap, List<MatchRecord> logList,BigDecimal inviterRate,List<RewardLog> rewardLog,List<ColaSystemBalance> feeLog) {
        BigDecimal makerFee;
        BigDecimal takerFee;
        String makerFeeCoinCode;
        String takerFeeCoinCode;
        switch (makerOrder.getType()) {
            case OrderType.LIMIT:
                // A_S  解冻S  增加 A(扣除手续费) 手续费加入系统账户
                if (makerOrder.direction.equals(OrderDirection.BUY)) {
                    makerFee = getFee(number,makerOrder.getFeeRate());
                    makerFeeCoinCode = makerOrder.coinCode();
                    takerFee = getFee(price.multiply(number),takerOrder.getFeeRate());
                    takerFeeCoinCode = makerOrder.symbol();

                    BatchBalance user_symbol = balanceMap.computeIfAbsent(makerOrder.getUserId() + makerOrder.symbol(), k -> new BatchBalance(makerOrder.getUserId() + makerOrder.symbol()));
                    user_symbol.setFrozen(user_symbol.getFrozen().subtract(price.multiply(number)));

                    BatchBalance user_coin = balanceMap.computeIfAbsent(makerOrder.getUserId() + makerOrder.coinCode(), k -> new BatchBalance(makerOrder.getUserId() + makerOrder.coinCode()));
                    user_coin.setAvailable(user_coin.getAvailable().add(number.subtract(makerFee)));

                    dealFeeAndReward(makerOrder.coinCode(),makerFee,balanceMap,inviterRate,rewardLog,feeLog,makerOrder.getUserId());

                } else {
                    // 解冻 A   增加 S
                    BigDecimal total = price.multiply(number);
                    makerFee = getFee(total,makerOrder.getFeeRate());
                    makerFeeCoinCode = makerOrder.symbol();
                    takerFee = getFee(number,takerOrder.getFeeRate());
                    takerFeeCoinCode = makerOrder.coinCode();

                    BatchBalance user_coin = balanceMap.computeIfAbsent(makerOrder.getUserId() + makerOrder.coinCode(), k -> new BatchBalance(makerOrder.getUserId() + makerOrder.coinCode()));
                    user_coin.setFrozen(user_coin.getFrozen().subtract(number));

                    BatchBalance user_symbol = balanceMap.computeIfAbsent(makerOrder.getUserId() + makerOrder.symbol(), k -> new BatchBalance(makerOrder.getUserId() + makerOrder.symbol()));
                    user_symbol.setAvailable(user_symbol.getAvailable().add(total.subtract(makerFee)));

                    dealFeeAndReward(makerOrder.symbol(),makerFee,balanceMap,inviterRate,rewardLog,feeLog,makerOrder.getUserId());

                }
                // 添加日志
                logList.add(createOrderMatchRecord(makerOrder.getId(),makerOrder.getPair(),makerFee,makerFeeCoinCode, MatchRecordType.MAKER, price, number, timestamp,makerOrder.getDirection(), makerOrder.getUserId()));
                logList.add(createOrderMatchRecord(takerOrder.getId(),takerOrder.getPair(),takerFee,takerFeeCoinCode, MatchRecordType.TAKER, price, number, timestamp,takerOrder.getDirection(),takerOrder.getUserId()));
                break;
            default:
                throw new RuntimeException("未实现的订单类型: " + makerOrder.type);
        }
    }


    /**
     * 吃单
     * @param takerOrder
     * @param takerStatus
     * @param totalSpent
     * @param totalAmount
     * @param balanceMap
     * @param inviterRate
     * @param rewardLog
     * @param feeLog
     */
    private void clearTakerOrder(OrderMessage takerOrder, String takerStatus, BigDecimal totalSpent, BigDecimal totalAmount, Map<String, BatchBalance> balanceMap,  BigDecimal inviterRate,List<RewardLog> rewardLog,List<ColaSystemBalance> feeLog,BigDecimal takerRemain) {
        BigDecimal takerFee;
        switch (takerOrder.getType()){
            case OrderType.LIMIT:
                // A_S 解冻S 返还S 增加 A
                if (takerOrder.direction.equals(OrderDirection.BUY)){
                    takerFee = getFee(totalAmount,takerOrder.getFeeRate());

                    BatchBalance user_symbol = balanceMap.computeIfAbsent(takerOrder.getUserId() + takerOrder.symbol(), k -> new BatchBalance(takerOrder.getUserId() + takerOrder.symbol()));
                    BigDecimal takerTotal = takerOrder.getPrice().multiply(takerOrder.getNumber());// 总冻结
                    BigDecimal makerRemain = takerOrder.getPrice().multiply(takerRemain); // 剩余挂单
                    user_symbol.setFrozen(user_symbol.getFrozen().subtract(takerTotal).add(makerRemain)); // 剩余未撮合数量 * 撮合价格
                    user_symbol.setAvailable(user_symbol.getAvailable().add(takerTotal).subtract(totalSpent).subtract(makerRemain));

                    BatchBalance user_coin = balanceMap.computeIfAbsent(takerOrder.getUserId() + takerOrder.coinCode(), k -> new BatchBalance(takerOrder.getUserId() + takerOrder.coinCode()));
                    user_coin.setAvailable(user_coin.getAvailable().add(totalAmount.subtract(takerFee)));

                    dealFeeAndReward(takerOrder.coinCode(),takerFee,balanceMap,inviterRate,rewardLog,feeLog,takerOrder.getUserId());
                } else {
                    // A_S 解冻 A   增加 S
                    takerFee = getFee(totalSpent,takerOrder.getFeeRate());

                    BatchBalance user_coin = balanceMap.computeIfAbsent(takerOrder.getUserId() + takerOrder.coinCode(), k -> new BatchBalance(takerOrder.getUserId() + takerOrder.coinCode()));
                    user_coin.setFrozen(user_coin.getFrozen().subtract(totalAmount));

                    BatchBalance user_symbol = balanceMap.computeIfAbsent(takerOrder.getUserId() + takerOrder.symbol(), k -> new BatchBalance(takerOrder.getUserId() + takerOrder.symbol()));
                    user_symbol.setAvailable(user_symbol.getAvailable().add(totalSpent.subtract(takerFee)));

                    dealFeeAndReward(takerOrder.symbol(),takerFee,balanceMap,inviterRate,rewardLog,feeLog,takerOrder.getUserId());
                }
                // 加权平均价
                BigDecimal averagePrice = totalSpent.add(takerOrder.getPrice().multiply(takerRemain))
                        .divide(takerOrder.getNumber(),8, RoundingMode.HALF_UP);
                takerOrder.setAveragePrice(averagePrice);
                break;

            default:
                throw new RuntimeException("未实现的订单类型: " + takerOrder.type);
        }


    }


    /**
     * 取消订单
     * @param msg
     * @param orderMap
     * @param balanceMap
     */
    private void processCanceled(MatchMessage msg, Map<String,OrderMessage> orderMap, Map<String,BatchBalance> balanceMap) {
        String coinCode;
        BigDecimal frozenNumber;
        OrderMessage order = orderMap.get(msg.getOrderId());
        if (OrderDirection.BUY.equals(order.getDirection())){
            coinCode = order.symbol();
            frozenNumber = order.getPrice().multiply(order.getRemain());
        } else {
            coinCode = order.coinCode();
            frozenNumber = order.getRemain();
        }
        BatchBalance balance = balanceMap.computeIfAbsent(order.getUserId() + coinCode, k -> new BatchBalance(order.getUserId() + coinCode));
        balance.setFrozen(balance.getFrozen().subtract(frozenNumber));
        balance.setAvailable(balance.getAvailable().add(frozenNumber));
    }


    /**
     * 处理手续费和邀请奖励
     * @param coinCode
     * @param fee
     * @param balanceMap
     * @param inviterRate
     * @param rewardLog
     * @param feeLog
     * @param userId
     */
    private void dealFeeAndReward(String coinCode,BigDecimal fee,Map<String, BatchBalance> balanceMap,BigDecimal inviterRate,List<RewardLog> rewardLog,List<ColaSystemBalance> feeLog,String userId){
        if (fee.compareTo(BigDecimal.ZERO) == 0) return;
        String inviter = inviterMap.get(userId);
        BatchBalance sys_coin = balanceMap.computeIfAbsent(UserConstant.SYS_ACCOUNT_ID + coinCode, k -> new BatchBalance(UserConstant.SYS_ACCOUNT_ID + coinCode));
        if (inviterRate.compareTo(BigDecimal.ZERO) != 0 && StringUtils.isNotBlank(inviter)){
            BigDecimal reward = fee.multiply(inviterRate);
            BatchBalance inviter_balance = balanceMap.computeIfAbsent(inviter + coinCode, k -> new BatchBalance(inviter+ coinCode));
            inviter_balance.setAvailable(inviter_balance.getAvailable().add(reward));
            rewardLog.add(createRewardLog(inviter,coinCode,reward));
            fee = fee.subtract(reward);
        }
        sys_coin.setAvailable(sys_coin.getAvailable().add(fee));
        feeLog.add(createSystemBalance(userId,coinCode,fee));
    }

    private ColaSystemBalance createSystemBalance(String fromUserId,String coinCode,BigDecimal number){
        ColaSystemBalance balance = new ColaSystemBalance();
        balance.setId(UUID.randomUUID().toString());
        balance.setToUser(UserConstant.SYS_ACCOUNT_ID);
        balance.setCoinCode(coinCode);
        balance.setAmount(number);
        balance.setFromUser(fromUserId);
        balance.setAction("in");
        balance.setType(SystemBalanceConstant.FEES_TRANSACTION);
        balance.setDescription("交易手续费");
        balance.setTime(System.currentTimeMillis());
        return balance;
    }
    private RewardLog createRewardLog(String userId,String coinCode,BigDecimal number){
        RewardLog log = new RewardLog();
        log.setId(UUID.randomUUID().toString());
        log.setUserId(userId);
        log.setCoinCode(coinCode);
        log.setCount(number);
        log.setTime(System.currentTimeMillis());
        return log;
    }


    private MatchRecord createOrderMatchRecord(String id,String pair,BigDecimal fee,String feeCoinCode, String type, BigDecimal price, BigDecimal number, long timestamp,String direction,String userId) {
        MatchRecord record = new MatchRecord();
        record.setId(UUID.randomUUID().toString());
        record.setPair(pair);
        record.setType(type);
        record.setPrice(price);
        record.setNumber(number);
        record.setTimestamp(timestamp);
        record.setOrderId(id);
        record.setFee(fee);
        record.setFeeCoinCode(feeCoinCode);
        record.setDirection(direction);
        record.setUserId(userId);
        return record;
    }

    private static BigDecimal getFee(BigDecimal number,BigDecimal feeRate){
        return number.multiply(feeRate);
    }

}
