package com.bitcola.exchange.launchpad.biz;

import com.bitcola.exchange.launchpad.constant.ColaIsoUnlockType;
import com.bitcola.exchange.launchpad.entity.ColaResonance;
import com.bitcola.exchange.launchpad.entity.ColaResonanceInviterRewardLog;
import com.bitcola.exchange.launchpad.entity.ColaResonanceUnlockLog;
import com.bitcola.exchange.launchpad.mapper.ColaResonanceDestroyMapper;
import com.bitcola.exchange.launchpad.mapper.ColaResonanceInviterRewardLogMapper;
import com.bitcola.exchange.launchpad.mapper.ColaResonanceMapper;
import com.bitcola.exchange.launchpad.project.ResonanceProject;
import com.bitcola.exchange.launchpad.vo.*;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.util.TimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * @author zkq
 * @create 2019-05-12 10:24
 **/
@Service
@Transactional
public class ColaResonanceBiz {

    public static final Map<String, ResonanceProject> projects = new HashMap<>();

    @Autowired
    ColaResonanceMapper mapper;

    @Autowired
    ColaResonanceDestroyMapper destroyMapper;

    @Autowired
    ColaResonanceInviterRewardLogMapper inviterRewardLogMapper;





    public BigDecimal calculate(BigDecimal amount,String coinCode) {
        ResonanceProject project = getProjectServer(coinCode);
        BigDecimal count = this.getTotalNumber(coinCode);
        int currentRound = project.getCurrentRound(count);
        BigDecimal currentRoundPrice = project.getPriceByRound(currentRound);
        BigDecimal currentRoundRemain = project.getCurrentRoundRemain(count);
        BigDecimal buyNumber = amount.divide(currentRoundPrice, 2, RoundingMode.DOWN);
        buyNumber =  buyNumber.min(currentRoundRemain);
        BigDecimal totalNumber = buyNumber;
        BigDecimal buyAmount = buyNumber.multiply(currentRoundPrice);
        BigDecimal remainAmount = amount.subtract(buyAmount);
        while (buyNumber.compareTo(BigDecimal.ZERO) != 0){
            if (currentRound == project.getTotalRound()) break;
            currentRound++;
            currentRoundPrice = project.getPriceByRound(currentRound);
            currentRoundRemain = project.getNumberByRound(currentRound);
            buyNumber = remainAmount.divide(currentRoundPrice, 2, RoundingMode.DOWN);
            buyNumber =  buyNumber.min(currentRoundRemain);
            totalNumber = totalNumber.add(buyNumber);
            buyAmount = buyNumber.multiply(currentRoundPrice);
            remainAmount = remainAmount.subtract(buyAmount);
            if (buyNumber.compareTo(BigDecimal.ZERO) == 0 ) break;
        }
        return totalNumber;
    }


    public Map<String, Object> statistics(String coinCode) {
        ResonanceProject project = getProjectServer(coinCode);
        BigDecimal capitalPool = mapper.capitalPool(coinCode).setScale(2,RoundingMode.HALF_UP);
        BigDecimal countNumber = this.getTotalNumber(coinCode).setScale(2,RoundingMode.HALF_UP);
        int currentRound = project.getCurrentRound(countNumber);
        BigDecimal conversionRatio = BigDecimal.ONE.divide(project.getPriceByRound(currentRound), 0, RoundingMode.DOWN);
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp",System.currentTimeMillis());
        // 资金池
        map.put("capitalPool",capitalPool);
        // 已共振数量
        map.put("countNumber",mapper.countNumber(coinCode).setScale(2,RoundingMode.HALF_UP));
        // 当前兑换比例
        map.put("conversionRatio",conversionRatio);
        // 当前阶段剩余
        map.put("total",project.getNumberByRound(currentRound));
        map.put("remain",project.getCurrentRoundRemain(countNumber));
        map.put("round",currentRound);
        map.put("startTime", this.getProjectStartTime(coinCode));
        map.put("endTime",this.getProjectEndTime(coinCode));
        map.put("coinCode",coinCode);
        map.put("symbol",project.symbol());
        map.put("capitalPoolCoinCode",project.getTotalNumber().subtract(countNumber));

        return map;
    }


    public long getWeekStartTimestamp(long startTime){
        long l = System.currentTimeMillis() - startTime;
        long week = 7 * 24 * 60 * 60 * 1000;
        return (l / week ) * week + startTime;
    }

    public List<ColaResonanceRankResponse> rank(Integer limit, String coinCode) {
        long weekStartTimestamp = getWeekStartTimestamp(this.getProjectStartTime(coinCode));
        List<ColaResonanceRankResponse> rank = mapper.rank(limit,weekStartTimestamp);
        for (int i = 0; i < rank.size(); i++) {
            ColaResonanceRankResponse response = rank.get(i);
            response.setIndex(i+1);
            response.setNumber(response.getNumber().setScale(4,RoundingMode.HALF_UP));
            if (rank.size()>=5){
                BigDecimal capitalPool = mapper.capitalPoolByWeek(coinCode,weekStartTimestamp).setScale(2, RoundingMode.DOWN);
                response.setReward(getRankReward(i+1,capitalPool).setScale(4,RoundingMode.DOWN));
            }
        }
        return rank;
    }

    private BigDecimal getRankReward(int rank,BigDecimal capitalPool) {
        if (rank > 5 || rank <= 0) return BigDecimal.ZERO;
        switch (rank) {
            case 1:
                return capitalPool.multiply(new BigDecimal("0.04"));
            case 2:
                return capitalPool.multiply(new BigDecimal("0.025"));
            case 3:
                return capitalPool.multiply(new BigDecimal("0.015"));
            case 4:
                return capitalPool.multiply(new BigDecimal("0.012"));
            default:
                return capitalPool.multiply(new BigDecimal("0.008"));
        }
    }

    public List<ColaResonanceLastResponse> last(Integer limit, String coinCode) {
        List<ColaResonanceLastResponse> last = mapper.last(limit,coinCode);
        for (ColaResonanceLastResponse response : last) {
            response.setNumber(response.getNumber().setScale(4,RoundingMode.HALF_UP));
        }
        return last;

    }


    public String getUserPin() {
        return mapper.getUserPin(BaseContextHandler.getUserID());
    }


    public boolean frozenBalance(BigDecimal amount,String symbol) {
        int count = mapper.frozenBalance(amount,BaseContextHandler.getUserID()+symbol, EncoderUtil.BALANCE_KEY);
        if (count == 1) return true;
        return false;
    }

    /**
     * 购买流程,一个阶段一个阶段的购买
     * @param message
     */
    public void buy(ResonanceBuyMessage message) {
        String coinCode = message.getCoinCode();
        ResonanceProject project = getProjectServer(coinCode);
        List<ColaResonance> record = new ArrayList<>();
        BigDecimal amount = message.getAmount();
        BigDecimal count = this.getTotalNumber(coinCode);
        int currentRound = project.getCurrentRound(count);
        BigDecimal currentRoundPrice = project.getPriceByRound(currentRound);
        BigDecimal currentRoundRemain = project.getCurrentRoundRemain(count);
        BigDecimal buyNumber = amount.divide(currentRoundPrice, 2, RoundingMode.DOWN);
        buyNumber =  buyNumber.min(currentRoundRemain);
        BigDecimal totalNumber = buyNumber;

        BigDecimal buyAmount = buyNumber.multiply(currentRoundPrice);
        BigDecimal remainAmount = amount.subtract(buyAmount);
        record.add(createColaIso(message.getCoinCode(),message.getSymbol(),currentRoundPrice,buyNumber,message.getUserId(),currentRound,0));
        while (buyNumber.compareTo(BigDecimal.ZERO) != 0){
            if (currentRound == project.getTotalRound()) break;
            currentRound++;
            currentRoundPrice = project.getPriceByRound(currentRound);
            currentRoundRemain = project.getNumberByRound(currentRound);
            buyNumber = remainAmount.divide(currentRoundPrice, 2, RoundingMode.DOWN);
            buyNumber =  buyNumber.min(currentRoundRemain);
            totalNumber = totalNumber.add(buyNumber);
            buyAmount = buyNumber.multiply(currentRoundPrice);
            remainAmount = remainAmount.subtract(buyAmount);
            if (buyNumber.compareTo(BigDecimal.ZERO) == 0 ) break;
            record.add(createColaIso(message.getCoinCode(),message.getSymbol(),currentRoundPrice,buyNumber,message.getUserId(),currentRound,0));
        }
        // 总购买金额
        BigDecimal totalAmount = amount.subtract(remainAmount);
        // 修改总余额,
        int i = mapper.subFrozenAndBack(message.getUserId() + message.getSymbol(), amount, remainAmount, EncoderUtil.BALANCE_KEY);
        if (i == 1){
            BigDecimal number = mapper.selectBalanceNumberById(message.getUserId()+coinCode);
            if (number == null){
                mapper.insertLockCoin(message.getUserId()+coinCode,totalNumber,coinCode,message.getUserId());
            } else {
                mapper.updateLockCoin(message.getUserId()+coinCode,totalNumber.add(number));
            }
        } else {
            throw new RuntimeException("资金错误:"+message.getUserId()+message.getSymbol());
        }
        // 记录日志
        mapper.record(record);
        // 推送
        List<ColaResonanceUnlockLog> logs = new ArrayList<>();
        // 邀请奖励和邀请解锁
        String inviterUserId = mapper.getInviterUserId(message.getUserId());
        if (StringUtils.isNotBlank(inviterUserId)){
            // 查询自己有没有 kyc
            Integer status = mapper.selectKycStatus(message.getUserId());
            if (status != null && status == 1){
                BigDecimal reward = totalNumber.multiply(project.inviterReward()); // 1% 奖励
                inviterRewardLogMapper.insertSelective(createInviterRewardLog(message.getUserId(),inviterUserId,amount,reward,coinCode,message.getSymbol()));
                mapper.addCoinCode(inviterUserId+coinCode,reward,EncoderUtil.BALANCE_KEY);
                // 解锁邀请人 25% 购买金额是数量
                Map<String, BigDecimal> lockNumberAndAmount = mapper.getTotalNumberAndAmount(inviterUserId);
                if (lockNumberAndAmount != null){
                    BigDecimal inviterNumber = lockNumberAndAmount.get("number");
                    BigDecimal inviterAmount = lockNumberAndAmount.get("amount");
                    BigDecimal inviterUnlockAmount = totalAmount.multiply(project.inviterUnlock()); // 10%解锁
                    BigDecimal inviterUnlockNumber = inviterNumber.multiply(inviterUnlockAmount).divide(inviterAmount,2,RoundingMode.HALF_UP);
                    Map<String,BigDecimal> map = mapper.getLockNumber(inviterUserId+coinCode);
                    inviterUnlockNumber = inviterUnlockNumber.min(map.get("lock")); // 取未解锁的部分
                    if (inviterUnlockNumber.compareTo(BigDecimal.ZERO)>0){
                        unlock(inviterUserId,inviterUnlockNumber,coinCode);
                        logs.add(createUnlockLog(inviterUserId, ColaIsoUnlockType.INVITER,null,0,inviterUnlockNumber,coinCode));
                    }
                }
            }
        }
        BigDecimal unLockNumber = totalNumber.multiply(project.unlock()); // 立即解锁25%
        unlock(message.getUserId(),unLockNumber,coinCode);
        logs.add(createUnlockLog(message.getUserId(), ColaIsoUnlockType.AUTO,null,0,unLockNumber,coinCode));
        // 存日志
        if (logs.size() > 0){
            mapper.batchInsertUnlockLog(logs);
        }

    }

    private ColaResonance createColaIso(String coinCode, String symbol, BigDecimal price, BigDecimal number, String userId, Integer round, Integer stage){
        ColaResonance iso = new ColaResonance();
        iso.setId(UUID.randomUUID().toString());
        iso.setCoinCode(coinCode);
        iso.setSymbol(symbol);
        iso.setPrice(price);
        iso.setNumber(number);
        iso.setAmount(iso.getPrice().multiply(iso.getNumber()));
        iso.setUserId(userId);
        iso.setTimestamp(System.currentTimeMillis());
        iso.setRound(round);
        iso.setStage(stage);
        return iso;
    }

    public boolean isEnd(String coinCode){
        ResonanceProject project = getProjectServer(coinCode);
        BigDecimal count = this.getTotalNumber(coinCode);
        int currentRound = project.getCurrentRound(count);
        if (currentRound == project.getTotalRound()) {
            BigDecimal remain = project.getCurrentRoundRemain(count);
            if (remain.compareTo(BigDecimal.ZERO) == 0) return true;
        }
        return false;
    }

    public BigDecimal getTotalNumber(String coinCode){
        BigDecimal count = mapper.countNumber(coinCode);
        BigDecimal destroyNumber = destroyMapper.countNumber(coinCode);
        return count.add(destroyNumber);
    }


    /**
     * 解锁
     */
    private void unlock(String userId,BigDecimal unLockNumber,String coinCode){
        mapper.subLockNumber(userId+coinCode,unLockNumber);
        mapper.addCoinCode(userId+coinCode,unLockNumber,EncoderUtil.BALANCE_KEY);
    }

    private ColaResonanceUnlockLog createUnlockLog(String userId, String type, Integer inviterStage, Integer userStage, BigDecimal number,String coinCode){
        ColaResonanceUnlockLog log = new ColaResonanceUnlockLog();
        log.setId(UUID.randomUUID().toString());
        log.setCoinCode(coinCode);
        log.setUserId(userId);
        log.setType(type);
        log.setInviterStage(inviterStage);
        log.setUserStage(userStage);
        log.setTimestamp(System.currentTimeMillis());
        log.setNumber(number);
        return log;
    }

    /**
     * 按照时间自动解锁 1%
     * 修改为每天执行
     */
    @Scheduled(cron = "0 0 18 * * ?")
    public void timeUnlock(){
        List<ColaResonanceUnlockLog> logs = new ArrayList<>();
        // 获得所有的购买用户
        List<ResponseProjectListVo> list = mapper.resonanceList();
        for (ResponseProjectListVo project : list) {
            List<Map<String,Object>> locks = mapper.getUsersLockNumber(project.getCoinCode());
            for (Map<String, Object> lock : locks) {
                String userId = lock.get("user").toString();
                BigDecimal lockNumber = (BigDecimal) lock.get("lock");
                BigDecimal total = lockNumber.add((BigDecimal) lock.get("unlock"));
                BigDecimal unlockNumber = total.multiply(new BigDecimal("0.01"));
                unlockNumber = unlockNumber.min(lockNumber);
                unlock(userId,unlockNumber,project.getCoinCode());
                logs.add(createUnlockLog(userId, ColaIsoUnlockType.TIME,null,null,unlockNumber,project.getCoinCode()));
            }
        }
        // 存日志
        if (logs.size() > 0){
            mapper.batchInsertUnlockLog(logs);
        }
    }


    public Map<String, Object> getLockNumber(String userID,String coinCode) {
        BigDecimal available = mapper.getUserBalance(userID,coinCode);
        Map<String,BigDecimal> map = mapper.getLockNumber(userID+coinCode);
        Map<String, Object> result = new HashMap<>();
        result.put("available",available.setScale(3,RoundingMode.DOWN));
        result.put("coinCode",coinCode);
        if (map != null){
            result.put("lock",map.get("lock").setScale(3,RoundingMode.DOWN));
            result.put("unlock",map.get("unlock").setScale(3,RoundingMode.DOWN));
        } else {
            result.put("lock",0);
            result.put("unlock",0);
        }
        return result;
    }

    public List<ColaResonanceUnlockLog> getUnlockDetail(String userId,String coinCode) {
        List<ColaResonanceUnlockLog> log = mapper.getUnlockDetail(userId,coinCode);
        return log;
    }
    private ColaResonanceInviterRewardLog createInviterRewardLog(String userId, String inviterUserId, BigDecimal amount, BigDecimal reward,String coinCode,String symbol){
        ColaResonanceInviterRewardLog log = new ColaResonanceInviterRewardLog();
        log.setId(UUID.randomUUID().toString());
        log.setTimestamp(System.currentTimeMillis());
        log.setCoinCode(coinCode);
        log.setSymbol(symbol);
        log.setUserId(userId);
        log.setInviterUserId(inviterUserId);
        log.setAmount(amount);
        log.setReward(reward);

        return log;
    }


    public void insertRecord(ColaResonance iso) {
        List<ColaResonance> list = new ArrayList<>();
        list.add(iso);
        mapper.record(list);
    }

    public Map<String, Object> dealDahu(String userId,BigDecimal amount, Integer round,String coinCode) {
        ResonanceProject project = getProjectServer(coinCode);
        List<ColaResonanceUnlockLog> logs = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        BigDecimal price = project.getPriceByRound(round);
        BigDecimal number = amount.divide(price, 2, RoundingMode.DOWN);
        BigDecimal dbNumber = mapper.selectBalanceNumberById(userId+coinCode);
        if (dbNumber == null){
            mapper.insertLockCoin(userId+coinCode,number,coinCode,project.symbol());
        } else {
            mapper.updateLockCoin(userId+coinCode,number.add(dbNumber));
        }
        BigDecimal unLockNumber = number.multiply(project.unlock()); // 立即解锁25%
        unlock(userId,unLockNumber,coinCode);
        logs.add(createUnlockLog(userId, ColaIsoUnlockType.AUTO,null,round,unLockNumber,coinCode));
        // 存日志
        if (logs.size() > 0){
            mapper.batchInsertUnlockLog(logs);
        }
        result.put("price",price.stripTrailingZeros().toPlainString());
        result.put("number",number.stripTrailingZeros().toPlainString());
        result.put("unLockNumber",unLockNumber.stripTrailingZeros().toPlainString());
        return result;

    }

    public List<Map<String,Object>> unlockPercent(String userId,Integer day,String coinCode) {
        List<Map<String,Object>> resultList = new ArrayList<>();
        for (int i = 0; i < day; i++) {
            List<ColaResonanceUnlockLog> logs = new ArrayList<>();
            Map<String, Object> result = new HashMap<>();
            Map<String, BigDecimal> map = mapper.getLockNumber(userId + coinCode);
            if (map == null){
                return resultList;
            }
            BigDecimal lock = map.get("lock");
            BigDecimal unlock = map.get("unlock");
            BigDecimal total = lock.add(unlock);
            BigDecimal unlockNumber = total.multiply(new BigDecimal("0.01"));
            unlockNumber = unlockNumber.min(lock);
            if (unlockNumber.compareTo(BigDecimal.ZERO) > 0){
                unlock(userId,unlockNumber,coinCode);
                logs.add(createUnlockLog(userId, ColaIsoUnlockType.TIME,null,null,unlockNumber,coinCode));
                if (logs.size() > 0){
                    mapper.batchInsertUnlockLog(logs);
                }
                result.put("unLockNumber",unlockNumber.stripTrailingZeros().toPlainString());
                resultList.add(result);
            }
        }
        return resultList;
    }


    /**
     * 状态,
     * @return
     */
    public List<ResponseProjectListVo> resonanceList(Integer processing) {
        List<ResponseProjectListVo> list = mapper.resonanceList();
        for (ResponseProjectListVo vo : list) {
            ResonanceProject project = getProjectServer(vo.getCoinCode());
            vo.setTimestamp(System.currentTimeMillis());
            if (vo.getTimestamp() < vo.getStartTime()){
                vo.setStatus("WAITING");
            } else if (vo.getTimestamp() > vo.getEndTime()){
                vo.setStatus("END");
            } else {
                vo.setStatus("PROCESSING");
            }
            // 基金池
            vo.setCapitalPool(this.capitalPool(vo.getCoinCode()));
            // 当前共振比例
            vo.setConversionRatio(this.conversionRatio(vo.getCoinCode()));
            vo.setCountNumber(this.countNumber(vo.getCoinCode()));
            BigDecimal countNumber = this.countNumber(vo.getCoinCode());
            vo.setRound(project.getCurrentRound(countNumber));
            vo.setTotal(project.getNumberByRound(vo.getRound()));
            vo.setRemain(project.getCurrentRoundRemain(countNumber));
            vo.setCapitalPoolCoinCode(project.getTotalNumber().subtract(countNumber));
        }
        if (processing!=null && processing == 1){
            Iterator<ResponseProjectListVo> iterator = list.iterator();
            while (iterator.hasNext()){
                if( !iterator.next().getStatus().equalsIgnoreCase("PROCESSING")){
                    iterator.remove();
                }
            }
        }
        return list;
    }

    public ResonanceProject getProjectServer(String coinCode){
        return projects.get(coinCode);
    }

    /**
     *
     * @param coinCode
     * @return
     */
    private BigDecimal conversionRatio(String coinCode) {
        ResonanceProject project = getProjectServer(coinCode);
        int currentRound = project.getCurrentRound(countNumber(coinCode));
        BigDecimal price = project.getPriceByRound(currentRound);
        return BigDecimal.ONE.divide(price,0,RoundingMode.DOWN);

    }

    private BigDecimal capitalPool(String coinCode) {
        return mapper.capitalPool(coinCode).setScale(2,RoundingMode.HALF_UP);
    }
    private BigDecimal countNumber(String coinCode) {
        return mapper.countNumber(coinCode);
    }

    /**
     * 详情
     * @param coinCode
     * @return
     */
    public Map<String, Object> resonanceDetail(String coinCode) {
        ResonanceProject project = getProjectServer(coinCode);
        Map<String, Object> map = this.statistics(coinCode);
        // 剩余资金 LDS
        map.put("minNumber",BigDecimal.ONE);
        int round = (int) map.get("round");

        List<Map<String,Object>> list = new ArrayList<>();
        for (round++ ;round <= project.getTotalRound();round++) {
            Map<String,Object> roundMap = new HashMap<>();
            roundMap.put("round",round);
            roundMap.put("total",project.getNumberByRound(round));
            BigDecimal conversionRatio = BigDecimal.ONE.divide(project.getPriceByRound(round), 2, RoundingMode.DOWN);
            roundMap.put("conversionRatio",conversionRatio);
            list.add(roundMap);
            if (list.size()>=2) break;
        }
        map.put("rounds",list);
        return map;
    }

    public long getProjectStartTime(String coinCode){
        return mapper.getProjectStartTime(coinCode);
    }

    public long getProjectEndTime(String coinCode){
        return mapper.getProjectEndTime(coinCode);
    }

}
