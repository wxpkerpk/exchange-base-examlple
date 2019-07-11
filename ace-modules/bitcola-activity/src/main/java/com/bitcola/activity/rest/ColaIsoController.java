package com.bitcola.activity.rest;

import com.bitcola.activity.biz.ColaIsoBiz;
import com.bitcola.activity.entity.ColaIso;
import com.bitcola.activity.entity.ColaIsoInviterRewardLog;
import com.bitcola.activity.entity.ColaIsoUnlockLog;
import com.bitcola.activity.mapper.ColaIsoInviterRewardLogMapper;
import com.bitcola.activity.msg.BuyMessage;
import com.bitcola.activity.util.ColaIsoUtil;
import com.bitcola.activity.vo.ColaIsoLastResponse;
import com.bitcola.activity.vo.ColaIsoRankResponse;
import com.bitcola.exchange.security.auth.client.annotation.IgnoreUserToken;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * iso相关接口
 * @author zkq
 * @create 2019-05-11 14:42
 **/
@RestController
@RequestMapping("iso")
public class ColaIsoController {

    @Autowired
    BlockingQueue<BuyMessage> buyQueue;

    @Autowired
    ColaIsoBiz biz;

    @Autowired
    ColaIsoInviterRewardLogMapper rewardLogMapper;

    /**
     * 首页五个数据 (统计+推送)
     * @return
     */
    @IgnoreUserToken
    @RequestMapping("statistics")
    public AppResponse statistics(){
        Map<String,Object> result = biz.statistics();
        return AppResponse.ok().data(result);
    }

    /**
     * 买入
     */
    @RequestMapping(value = "buy",method = RequestMethod.POST)
    public AppResponse buy(@RequestBody BuyMessage params){
        if (params == null) return AppResponse.paramsError();
        if (StringUtils.isBlank(params.getPin()) || params.getAmount().compareTo(BigDecimal.ONE)<0)
            return AppResponse.paramsError();
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis < biz.getStartTime()) return AppResponse.error(ResponseCode.COIN_NOT_OPEN,ResponseCode.COIN_NOT_OPEN_MESSAGE);
        if (biz.isEnd()) return AppResponse.error(9881,ResponseCode.COIN_NOT_OPEN_MESSAGE);
        int stage = biz.getCurrentStage();
        long endTimestamp = ColaIsoUtil.getEndTimestamp(stage);
        long startTimestamp = ColaIsoUtil.getStartTimestamp(stage);
        if (currentTimeMillis < startTimestamp || currentTimeMillis > endTimestamp ) {
            Map<String,Long> time = new HashMap<>();
            time.put("startTimestamp",startTimestamp);
            time.put("endTimestamp",endTimestamp);
            AppResponse response = AppResponse.error(ResponseCode.COIN_NOT_OPEN, ResponseCode.COIN_NOT_OPEN_MESSAGE);
            response.setData(time);
            return response;
        }

        // 验证用户pin
        String pin = biz.getUserPin();
        if (StringUtils.isBlank(pin)) return AppResponse.error(ResponseCode.NO_MONEY_PASSWORD_CODE,ResponseCode.NO_MONEY_PASSWORD_MESSAGE);
        if (!EncoderUtil.matches(params.getPin(),pin)) return AppResponse.error(ResponseCode.PIN_ERROR_CODE,ResponseCode.PIN_ERROR_MESSAGE);
        // 验证用户余额是否充足
        boolean success = biz.frozenBalance(params.getAmount());
        if (!success) return AppResponse.error(ResponseCode.NO_ENOUGH_MONEY_CODE,ResponseCode.NO_ENOUGH_MONEY_MESSAGE);
        params.setCoinCode(biz.getCoinCode());
        params.setSymbol(biz.getSymbol());
        params.setUserId(BaseContextHandler.getUserID());
        buyQueue.offer(params);
        return AppResponse.ok();
    }


    /**
     * 排名 (统计+推送)
     */
    @IgnoreUserToken
    @RequestMapping("rank")
    public AppResponse rank(Integer limit,String type){
        if (StringUtils.isBlank(type)) return AppResponse.paramsError();
        if (!"person".equals(type) && !"inviter".equals(type)) return AppResponse.paramsError();
        if (limit == null) limit = 20;
        if (limit > 20) limit = 20;
        List<ColaIsoRankResponse> result = biz.rank(limit,type);
        return AppResponse.ok().data(result);
    }
    /**
     * 自己的排名
     */
    @RequestMapping("rankSelf")
    public AppResponse rankSelf(){
        String userID = BaseContextHandler.getUserID();
        ColaIsoRankResponse result = biz.rankSelf(userID);
        return AppResponse.ok().data(result);
    }
    /**
     * 自己的资金情况
     */
    @RequestMapping("getLockNumber")
    public AppResponse getLockNumber(){
        String userID = BaseContextHandler.getUserID();
        Map<String,Object> result = biz.getLockNumber(userID);
        return AppResponse.ok().data(result);
    }

    /**
     * 解锁情况
     */
    @RequestMapping("getUnlockDetail")
    public AppResponse getUnlockDetail(){
        String userID = BaseContextHandler.getUserID();
        List<ColaIsoUnlockLog> result = biz.getUnlockDetail(userID);
        return AppResponse.ok().data(result);
    }

    /**
     * 邀请奖励
     */
    @RequestMapping("inviterRewardLog")
    public AppResponse inviterRewardLog(){
        String userID = BaseContextHandler.getUserID();
        List<ColaIsoInviterRewardLog> result = rewardLogMapper.inviterRewardLog(userID);
        return AppResponse.ok().data(result);
    }


    /**
     * 最新共振 (统计+推送)
     */
    @IgnoreUserToken
    @RequestMapping("last")
    public AppResponse last(Integer limit){
        if (limit == null) limit = 20;
        if (limit > 20) limit = 20;
        List<ColaIsoLastResponse> result = biz.last(limit);
        return AppResponse.ok().data(result);
    }

    /**
     * 服务器时间,阶段,价格,数量等相关数据  (统计+推送)
     *      最新 3 个阶段价格,剩余,总共
     * @return
     */
    @IgnoreUserToken
    @RequestMapping("getInfo")
    public AppResponse getInfo(){
        Map<String,Object> result = biz.getInfo();
        return AppResponse.ok().data(result);
    }


    /**
     * 实时计算当前购买的数量
     * @param amount
     * @return
     */
    @IgnoreUserToken
    @RequestMapping("calculate")
    public AppResponse calculate(BigDecimal amount){
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return AppResponse.paramsError();
        BigDecimal result = biz.calculate(amount);
        return AppResponse.ok().data(result);
    }

    public static final List<Integer> clearStageCache = new ArrayList<>();

    @IgnoreUserToken
    @RequestMapping("clear")
    public AppResponse clear(Integer stage,String key,boolean force){
        if (!"kaiqiu".equals(key)){
            return AppResponse.error("");
        }
        int currentStage = biz.getCurrentStage();
        if (!force){
            if (currentStage != stage) return AppResponse.error("当前是第:"+currentStage+"轮,而不是:"+stage);
        }
        if (clearStageCache.contains(stage)){
            return AppResponse.error("已经清理过了:"+stage);
        } else {
            clearStageCache.add(stage);
        }
        biz.clearStage(stage);
        return AppResponse.ok();


    }

    @IgnoreUserToken
    @RequestMapping("change")
    public AppResponse change(Long startTime,Long l,Long s){
        ColaIsoUtil.change(startTime,l,s);
        return AppResponse.ok();

    }

    @IgnoreUserToken
    @RequestMapping("randomSomeRecord")
    public AppResponse randomSomeRecord(String key,String userId,BigDecimal amount){
        if (!"kaiqiu".equals(key)){
            return AppResponse.error("");
        }
        if (amount.compareTo(new BigDecimal("20"))>=0){
            return AppResponse.error("金额过大");
        }
        BigDecimal number = biz.calculate(amount);
        ColaIso iso = new ColaIso();
        iso.setId(UUID.randomUUID().toString());
        iso.setCoinCode(biz.getCoinCode());
        iso.setSymbol(biz.getSymbol());
        iso.setTimestamp(System.currentTimeMillis());
        iso.setAmount(amount);
        iso.setNumber(number);
        iso.setUserId(userId);
        iso.setStage(0);
        iso.setRound(0);
        iso.setPrice(BigDecimal.ZERO);
        biz.insertRecord(iso);
        return AppResponse.ok();

    }

    /**
     * 处理大户
     * @return
     */
    @RequestMapping("dealDahu")
    public AppResponse dealDahu(String userId,BigDecimal amount,Integer stage,String key){
        if (!"kaiqiu".equals(key)){
            return AppResponse.error("");
        }
        String userID = BaseContextHandler.getUserID();
        Integer id = Integer.valueOf(userID);
        if (id >= 100000) return AppResponse.error(ColaLanguage.FORBIDDEN);
        Map<String,Object> result = biz.dealDahu(userId,amount,stage);
        return AppResponse.ok().data(result);
    }


    /**
     * 立即解锁 1 %
     * @return
     */
    @RequestMapping("unlockPercent")
    public AppResponse unlockPercent(String userId,String key,Integer day){
        if (!"kaiqiu".equals(key)){
            return AppResponse.error("");
        }
        if (day == null) day = 1;
        String userID = BaseContextHandler.getUserID();
        Integer id = Integer.valueOf(userID);
        if (id >= 100000) return AppResponse.error(ColaLanguage.FORBIDDEN);
        List<Map<String,Object>> result = biz.unlockPercent(userId,day);
        return AppResponse.ok().data(result);
    }




}
