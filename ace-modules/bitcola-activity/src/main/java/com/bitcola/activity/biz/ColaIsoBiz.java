package com.bitcola.activity.biz;

import com.bitcola.activity.constant.ColaIsoUnlockType;
import com.bitcola.activity.entity.*;
import com.bitcola.activity.mapper.ColaIsoDestroyMapper;
import com.bitcola.activity.mapper.ColaIsoInviterRewardLogMapper;
import com.bitcola.activity.mapper.ColaIsoMapper;
import com.bitcola.activity.msg.BuyMessage;
import com.bitcola.activity.util.ColaIsoUtil;
import com.bitcola.activity.vo.ColaIsoLastResponse;
import com.bitcola.activity.vo.ColaIsoRankResponse;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author zkq
 * @create 2019-05-12 10:24
 **/
@Service
@Transactional
public class ColaIsoBiz {

    @Autowired
    ColaIsoMapper mapper;

    @Autowired
    ColaIsoDestroyMapper destroyMapper;

    @Autowired
    ColaIsoInviterRewardLogMapper inviterRewardLogMapper;

    BigDecimal totalNumber = new BigDecimal(1051061341);
    int maxRound = 1000;
    String symbol = "LTC";
    String coinCode = "LDS";
    String chainAddressPrefix = "https://ltc.ihashrate.com/address/";





    public BigDecimal calculate(BigDecimal amount) {
        BigDecimal count = this.getTotalNumber();
        int currentRound = getCurrentRound(count);
        BigDecimal currentRoundPrice = getCurrentRoundPrice(count);
        BigDecimal currentRoundRemain = getCurrentRoundRemain(count);
        BigDecimal buyNumber = amount.divide(currentRoundPrice, 2, RoundingMode.DOWN);
        buyNumber =  buyNumber.min(currentRoundRemain);
        BigDecimal totalNumber = buyNumber;
        int currentStage = getCurrentStage(currentRound);
        BigDecimal buyAmount = buyNumber.multiply(currentRoundPrice);
        BigDecimal remainAmount = amount.subtract(buyAmount);
        while (buyNumber.compareTo(BigDecimal.ZERO) != 0){
            if (currentRound == 1000) break;
            currentRound++;
            int stage = getCurrentStage(currentRound);
            if (stage!=currentStage) break;
            currentRoundPrice = getRoundPriceByRound(currentRound);
            currentRoundRemain = new BigDecimal(getRoundNumberByRound(currentRound));
            buyNumber = remainAmount.divide(currentRoundPrice, 2, RoundingMode.DOWN);
            buyNumber =  buyNumber.min(currentRoundRemain);
            totalNumber = totalNumber.add(buyNumber);
            buyAmount = buyNumber.multiply(currentRoundPrice);
            remainAmount = remainAmount.subtract(buyAmount);
            if (buyNumber.compareTo(BigDecimal.ZERO) == 0 ) break;
        }
        return totalNumber;
    }


    private static BigDecimal getCurrentRoundRemain(BigDecimal count){
        for (int i = 0; i < 1000; i++) {
            int number = calculateNumber(i + 1);
            count = count.subtract(new BigDecimal(number));
            if (count.compareTo(BigDecimal.ZERO) < 0) return count.negate();
        }
        return BigDecimal.ZERO;
    }

    /**
     * 每一轮总价格
     */
    private static final BigDecimal avg = new BigDecimal("120");

    /**
     * 当前这轮价格
     * @param count 总购买数量
     * @return
     */
    private static BigDecimal getCurrentRoundPrice(BigDecimal count){
        return avg.divide(new BigDecimal(getCurrentRoundNumber(count)),8,RoundingMode.HALF_UP);
    }
    /**
     * 某一轮价格
     * @param round 轮数
     * @return
     */
    private static BigDecimal getRoundPriceByRound(int round){
        return avg.divide(new BigDecimal(getRoundNumberByRound(round)),8,RoundingMode.HALF_UP);
    }

    /**
     * 当前这轮数量
     * @param count 总购买数量
     * @return
     */
    private static int getCurrentRoundNumber(BigDecimal count){
        return calculateNumber(getCurrentRound(count));
    }
    /**
     * 某一轮数量
     * @param round 轮数
     * @return
     */
    private static int getRoundNumberByRound(int round){
        return calculateNumber(round);
    }

    public static int getCurrentStage(int currentRound){
        return (currentRound-1) / 100 +1;
    }
    public int getCurrentStage(){
        int currentRound = getCurrentRound(this.getTotalNumber());
        return (currentRound-1) / 100 +1;
    }
    public BigDecimal getPriceByStage(Integer stage){
        return getRoundPriceByRound((stage-1) * 100 + 1);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
         System.out.println(getRoundPriceByRound((i) * 100 + 1));

        }
    }

    /**
     * 当前第几轮
     * @param count 总购买数量
     */
    private static int getCurrentRound(BigDecimal count){
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < 1000; i++) {
            int number = calculateNumber(i + 1);
            total = total.add(new BigDecimal(number));
            if (total.compareTo(count) > 0) return i+1;
        }
        return 1000;
    }

    /**
     * 基础算法,根据轮数获得数量
     */
    private static int calculateNumber(int round) {
        double pow = Math.pow((round - 1000.00), 2.0);
        double res = 550000 - Math.pow(1.0 / 3*round , 2) - Math.pow(1.0 / 5*round , 2)
                - Math.pow(1.0 / 8*round, 2) - (25000 - pow)* 1.66234 + 45000;
        return (int)res;
    }

    public Map<String, Object> statistics() {
        BigDecimal capitalPool = mapper.capitalPool().setScale(2,RoundingMode.HALF_UP);
        BigDecimal countNumber = this.getTotalNumber().setScale(2,RoundingMode.HALF_UP);
        BigDecimal conversionRatio = BigDecimal.ONE.divide(getCurrentRoundPrice(countNumber), 2, RoundingMode.DOWN);
        int currentRound = getCurrentRound(countNumber);
        int currentStage = getCurrentStage(currentRound);
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp",System.currentTimeMillis());
        // 资金池
        map.put("capitalPool",capitalPool);
        // 已共振数量
        map.put("countNumber",mapper.countNumber().setScale(2,RoundingMode.HALF_UP));
        // 当前兑换比例
        map.put("conversionRatio",conversionRatio);
        // 当前阶段剩余
        map.put("total",getCurrentRoundNumber(countNumber));
        map.put("remain",getCurrentRoundRemain(countNumber));
        map.put("round",currentRound);
        map.put("stage",currentStage);
        map.put("startTime",ColaIsoUtil.getStartTimestamp(currentStage));
        map.put("endTime",ColaIsoUtil.getEndTimestamp(currentStage));
        map.put("coinCode",coinCode);
        map.put("symbol",symbol);
        map.put("capitalPoolCoinCode",totalNumber.subtract(countNumber));

        return map;
    }

    public Map<String, Object> getInfo() {
        Map<String, Object> map = this.statistics();
        // 剩余资金 LDS
        map.put("minNumber",BigDecimal.ONE);
        int round = (int) map.get("round");

        List<Map<String,Object>> list = new ArrayList<>();
        for (round++ ;round <= maxRound;round++) {
            Map<String,Object> roundMap = new HashMap<>();
            roundMap.put("round",round);
            roundMap.put("total",getRoundNumberByRound(round));
            BigDecimal conversionRatio = BigDecimal.ONE.divide(getRoundPriceByRound(round), 2, RoundingMode.DOWN);
            roundMap.put("conversionRatio",conversionRatio);
            list.add(roundMap);
            if (list.size()>=2) break;
        }
        map.put("rounds",list);
        return map;
    }


    public List<ColaIsoRankResponse> rank(Integer limit,String type) {
        List<ColaIsoRankResponse> rank;
        if ("inviter".equals(type)){
            rank = mapper.inviterRank(limit);
        } else {
            rank = mapper.rank(limit);
        }
        for (int i = 0; i < rank.size(); i++) {
            ColaIsoRankResponse response = rank.get(i);
            response.setIndex(i+1);
            response.setSymbol(symbol);
            response.setNumber(response.getNumber().setScale(4,RoundingMode.HALF_UP));
            if (rank.size()>=10){
                response.setReward(getRankReward(i+1).setScale(4,RoundingMode.DOWN));
            }
        }
        return rank;
    }

    private BigDecimal getRankReward(int rank) {
        BigDecimal capitalPool = mapper.capitalPool().setScale(2, RoundingMode.DOWN);
        BigDecimal total = capitalPool.multiply(new BigDecimal("0.7"));
        if (rank > 10 || rank <= 0) return BigDecimal.ZERO;
        switch (rank) {
            case 1:
                return total.multiply(new BigDecimal(0.08));
            case 2:
                return total.multiply(new BigDecimal(0.05));
            case 3:
                return total.multiply(new BigDecimal(0.025));
            case 4:
                return total.multiply(new BigDecimal(0.015));
            default:
                return total.multiply(new BigDecimal(0.005));
        }
    }

    public List<ColaIsoLastResponse> last(Integer limit) {
        List<ColaIsoLastResponse> last = mapper.last(limit);
        for (ColaIsoLastResponse response : last) {
            response.setSymbol(symbol);
            response.setNumber(response.getNumber().setScale(4,RoundingMode.HALF_UP));
        }
        return last;

    }

    public String getDepositAddress(String userId) {
        List<ColaIsoRankResponse> ranks = mapper.rank(10);
        for (ColaIsoRankResponse rank : ranks) {
            if (rank.getUserId().equals(userId)){
                String chainAddress = mapper.getDepositAddress(userId,symbol);
                return chainAddressPrefix+chainAddress;
            }
        }
        return null;
    }

    public String getUserPin() {
        return mapper.getUserPin(BaseContextHandler.getUserID());
    }

    public Long getStartTime() {
        return mapper.getStartTime();
    }

    public boolean frozenBalance(BigDecimal amount) {
        int count = mapper.frozenBalance(amount,BaseContextHandler.getUserID()+symbol, EncoderUtil.BALANCE_KEY);
        if (count == 1) return true;
        return false;
    }

    public String getCoinCode(){
        return coinCode;
    }
    public String getSymbol(){
        return symbol;
    }

    /**
     * 购买流程,一个阶段一个阶段的购买
     * @param message
     */
    public void buy(BuyMessage message) {
        List<ColaIso> record = new ArrayList<>();
        BigDecimal amount = message.getAmount();
        BigDecimal count = this.getTotalNumber();
        int currentRound = getCurrentRound(count);
        int currentStage = getCurrentStage(currentRound);
        BigDecimal currentRoundPrice = getCurrentRoundPrice(count);
        BigDecimal currentRoundRemain = getCurrentRoundRemain(count);
        BigDecimal buyNumber = amount.divide(currentRoundPrice, 2, RoundingMode.DOWN);
        buyNumber =  buyNumber.min(currentRoundRemain);
        BigDecimal totalNumber = buyNumber;

        BigDecimal buyAmount = buyNumber.multiply(currentRoundPrice);
        BigDecimal remainAmount = amount.subtract(buyAmount);
        record.add(createColaIso(message.getCoinCode(),message.getSymbol(),currentRoundPrice,buyNumber,message.getUserId(),currentRound,currentStage));
        while (buyNumber.compareTo(BigDecimal.ZERO) != 0){
            if (currentRound == 1000) break;
            currentRound++;
            int stage = getCurrentStage(currentRound);
            if (stage!=currentStage) break;
            currentRoundPrice = getRoundPriceByRound(currentRound);
            currentRoundRemain = new BigDecimal(getRoundNumberByRound(currentRound));
            buyNumber = remainAmount.divide(currentRoundPrice, 2, RoundingMode.DOWN);
            buyNumber =  buyNumber.min(currentRoundRemain);
            totalNumber = totalNumber.add(buyNumber);
            buyAmount = buyNumber.multiply(currentRoundPrice);
            remainAmount = remainAmount.subtract(buyAmount);
            if (buyNumber.compareTo(BigDecimal.ZERO) == 0 ) break;
            record.add(createColaIso(message.getCoinCode(),message.getSymbol(),currentRoundPrice,buyNumber,message.getUserId(),currentRound,stage));
        }
        // 总购买金额
        BigDecimal totalAmount = amount.subtract(remainAmount);
        // 修改总余额,
        int i = mapper.subFrozenAndBack(message.getUserId() + symbol, amount, remainAmount, EncoderUtil.BALANCE_KEY);
        if (i == 1){
            BigDecimal number = mapper.selectBalanceNumberById(message.getUserId()+coinCode);
            if (number == null){
                mapper.insertLockCoin(message.getUserId()+coinCode,totalNumber);
            } else {
                mapper.updateLockCoin(message.getUserId()+coinCode,totalNumber.add(number));
            }
        } else {
            throw new RuntimeException("资金错误:"+message.getUserId()+symbol);
        }
        // 记录日志
        mapper.record(record);
        // 推送
        List<ColaIsoUnlockLog> logs = new ArrayList<>();
        // todo 发奖励金 2.5%
        String inviterUserId = mapper.getInviterUserId(message.getUserId());
        if (StringUtils.isNotBlank(inviterUserId)){
            // 查询自己有没有 kyc
            Integer status = mapper.selectKycStatus(message.getUserId());
            if (status != null && status == 1){
                BigDecimal reward = totalNumber.multiply(new BigDecimal("0.025")); // 2.5% 奖励
                inviterRewardLogMapper.insertSelective(createInviterRewardLog(message.getUserId(),inviterUserId,amount,reward));
                mapper.addCoinCode(inviterUserId+coinCode,reward,EncoderUtil.BALANCE_KEY);
                // 解锁邀请人 25% 购买金额是数量
                Map<String, BigDecimal> lockNumberAndAmount = mapper.getTotalNumberAndAmount(inviterUserId);
                if (lockNumberAndAmount != null){
                    BigDecimal inviterNumber = lockNumberAndAmount.get("number");
                    BigDecimal inviterAmount = lockNumberAndAmount.get("amount");
                    BigDecimal inviterUnlockAmount = totalAmount.multiply(new BigDecimal("0.25")); // 25%解锁
                    BigDecimal inviterUnlockNumber = inviterNumber.multiply(inviterUnlockAmount).divide(inviterAmount,2,RoundingMode.HALF_UP);
                    Map<String,BigDecimal> map = mapper.getLockNumber(inviterUserId+coinCode);
                    inviterUnlockNumber = inviterUnlockNumber.min(map.get("lock")); // 取未解锁的部分
                    if (inviterUnlockNumber.compareTo(BigDecimal.ZERO)>0){
                        unlock(inviterUserId,inviterUnlockNumber);
                        logs.add(createUnlockLog(inviterUserId, ColaIsoUnlockType.INVITER,null,currentStage,inviterUnlockNumber));
                    }
                }
            }
        }
        BigDecimal unLockNumber = totalNumber.multiply(new BigDecimal("0.25")); // 立即解锁25%
        unlock(message.getUserId(),unLockNumber);
        logs.add(createUnlockLog(message.getUserId(), ColaIsoUnlockType.AUTO,null,currentStage,unLockNumber));
        // 存日志
        if (logs.size() > 0){
            mapper.batchInsertUnlockLog(logs);
        }

    }

    private ColaIso createColaIso(String coinCode,String symbol,BigDecimal price,BigDecimal number,String userId,Integer round,Integer stage){
        ColaIso iso = new ColaIso();
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

    public boolean isEnd(){
        BigDecimal count = this.getTotalNumber();
        int currentRound = getCurrentRound(count);
        if (currentRound == 1000) {
            BigDecimal remain = getCurrentRoundRemain(count);
            if (remain.compareTo(BigDecimal.ZERO) == 0) return true;
        }
        return false;
    }

    public ColaIsoRankResponse rankSelf(String userID) {
        ColaIsoRankResponse selfRank = mapper.selfRank(userID);
        if (selfRank == null){
            selfRank = new ColaIsoRankResponse();
            selfRank.setIndex(0);
            selfRank.setReward(BigDecimal.ZERO);
            selfRank.setSymbol(getSymbol());
            selfRank.setNumber(BigDecimal.ZERO);
            selfRank.setUserId(userID);
        } else {
            selfRank.setReward(getRankReward(selfRank.getIndex()).setScale(4,RoundingMode.DOWN));
            selfRank.setSymbol(getSymbol());
            selfRank.setNumber(selfRank.getNumber().setScale(4,RoundingMode.HALF_UP));
        }
        return selfRank;
    }

    public BigDecimal getTotalNumber(){
        BigDecimal count = mapper.countNumber();
        BigDecimal destroyNumber = destroyMapper.countNumber();
        return count.add(destroyNumber);
    }

    /**
     * 销毁当前这阶段未完成的
     * todo 修改为每晚 8 点执行
     */
    @Scheduled(cron = "2 0 20 * * ?")
    public void destroy(){
        BigDecimal totalNumber = getTotalNumber();
        int round = getCurrentRound(totalNumber);
        int currentStage = getCurrentStage(round);
        long endTimestamp = ColaIsoUtil.getEndTimestamp(currentStage);
        if (System.currentTimeMillis() < endTimestamp) return;
        BigDecimal number = getCurrentRoundRemain(totalNumber);
        while (true){
            round++;
            if (currentStage == getCurrentStage(round)){
                int numberByRound = getRoundNumberByRound(round);
                number = number.add(new BigDecimal(numberByRound));
            } else {
                break;
            }
        }
        ColaIsoDestroy isoDestroy = new ColaIsoDestroy();
        isoDestroy.setId(UUID.randomUUID().toString());
        isoDestroy.setNumber(number);
        isoDestroy.setStage(currentStage);
        isoDestroy.setTimestamp(System.currentTimeMillis());
        destroyMapper.insert(isoDestroy);

    }


    /**
     * 清算这一轮,解锁 25% 资金,并且有邀请别人再次解锁对应比例
     * @param currentStage
     */
    public void clearStage(int currentStage) {
        destroy();
    }

    /**
     * 解锁
     */
    private void unlock(String userId,BigDecimal unLockNumber){
        mapper.subLockNumber(userId+coinCode,unLockNumber);
        mapper.addCoinCode(userId+coinCode,unLockNumber,EncoderUtil.BALANCE_KEY);
    }

    private ColaIsoUnlockLog createUnlockLog(String userId,String type,Integer inviterStage,Integer userStage,BigDecimal number){
        ColaIsoUnlockLog log = new ColaIsoUnlockLog();
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
        List<ColaIsoUnlockLog> logs = new ArrayList<>();
        // 获得所有的购买用户
        List<Map<String,Object>> locks = mapper.getUsersLockNumber();
        for (Map<String, Object> lock : locks) {
            String userId = lock.get("id").toString().replace(coinCode,"");
            BigDecimal lockNumber = (BigDecimal) lock.get("lock");
            BigDecimal total = lockNumber.add((BigDecimal) lock.get("unlock"));
            BigDecimal unlockNumber = total.multiply(new BigDecimal("0.01"));
            unlockNumber = unlockNumber.min(lockNumber);
            unlock(userId,unlockNumber);
            logs.add(createUnlockLog(userId, ColaIsoUnlockType.TIME,null,null,unlockNumber));
        }
        // 存日志
        if (logs.size() > 0){
            mapper.batchInsertUnlockLog(logs);
        }
    }


    public Map<String, Object> getLockNumber(String userID) {
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

    public List<ColaIsoUnlockLog> getUnlockDetail(String userId) {
        List<ColaIsoUnlockLog> log = mapper.getUnlockDetail(userId);
        return log;
    }
    private ColaIsoInviterRewardLog createInviterRewardLog(String userId,String inviterUserId,BigDecimal amount,BigDecimal reward){
        ColaIsoInviterRewardLog log = new ColaIsoInviterRewardLog();
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

    /**
     * 发放第一轮邀请奖励
     */
    public void publicReward(){
        List<Map<String,Object>> roundOne =  mapper.getRoundOneReward();
        for (Map<String, Object> map : roundOne) {
            String userId = map.get("uid").toString();
            BigDecimal number = (BigDecimal) map.get("number");
            BigDecimal amount = (BigDecimal) map.get("amount");
            String inviterUserId = mapper.getInviterUserId(userId);
            if (StringUtils.isNotBlank(inviterUserId)) {
                BigDecimal reward = number.multiply(new BigDecimal("0.025")); // 2.5% 奖励
                inviterRewardLogMapper.insertSelective(createInviterRewardLog(userId, inviterUserId, amount, reward));
                mapper.addCoinCode(inviterUserId + coinCode, reward, EncoderUtil.BALANCE_KEY);
            }

        }
    }


    public void insertRecord(ColaIso iso) {
        List<ColaIso> list = new ArrayList<>();
        list.add(iso);
        mapper.record(list);
    }

    public Map<String, Object> dealDahu(String userId,BigDecimal amount, Integer stage) {
        List<ColaIsoUnlockLog> logs = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        BigDecimal price = this.getPriceByStage(stage);
        BigDecimal number = amount.divide(price, 2, RoundingMode.DOWN);
        BigDecimal dbNumber = mapper.selectBalanceNumberById(userId+coinCode);
        if (dbNumber == null){
            mapper.insertLockCoin(userId+coinCode,number);
        } else {
            mapper.updateLockCoin(userId+coinCode,number.add(dbNumber));
        }
        BigDecimal unLockNumber = number.multiply(new BigDecimal("0.25")); // 立即解锁25%
        unlock(userId,unLockNumber);
        logs.add(createUnlockLog(userId, ColaIsoUnlockType.AUTO,null,stage,unLockNumber));
        // 存日志
        if (logs.size() > 0){
            mapper.batchInsertUnlockLog(logs);
        }
        result.put("price",price.stripTrailingZeros().toPlainString());
        result.put("number",number.stripTrailingZeros().toPlainString());
        result.put("unLockNumber",unLockNumber.stripTrailingZeros().toPlainString());
        return result;

    }

    public List<Map<String,Object>> unlockPercent(String userId,Integer day) {
        List<Map<String,Object>> resultList = new ArrayList<>();
        for (int i = 0; i < day; i++) {
            List<ColaIsoUnlockLog> logs = new ArrayList<>();
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
                unlock(userId,unlockNumber);
                logs.add(createUnlockLog(userId, ColaIsoUnlockType.TIME,null,null,unlockNumber));
                if (logs.size() > 0){
                    mapper.batchInsertUnlockLog(logs);
                }
                result.put("unLockNumber",unlockNumber.stripTrailingZeros().toPlainString());
                resultList.add(result);
            }
        }
        return resultList;
    }
}
