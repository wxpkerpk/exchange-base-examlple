package com.bitcola.exchange.launchpad.rest;

import com.bitcola.exchange.launchpad.biz.ColaResonanceBiz;
import com.bitcola.exchange.launchpad.entity.ColaResonance;
import com.bitcola.exchange.launchpad.entity.ColaResonanceInviterRewardLog;
import com.bitcola.exchange.launchpad.entity.ColaResonanceUnlockLog;
import com.bitcola.exchange.launchpad.mapper.ColaResonanceInviterRewardLogMapper;
import com.bitcola.exchange.launchpad.project.ResonanceProject;
import com.bitcola.exchange.launchpad.vo.ResonanceBuyMessage;
import com.bitcola.exchange.launchpad.vo.ColaResonanceLastResponse;
import com.bitcola.exchange.launchpad.vo.ColaResonanceRankResponse;
import com.bitcola.exchange.launchpad.vo.ResponseProjectListVo;
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
@RequestMapping("resonance")
public class ColaResonanceController {

    @Autowired
    BlockingQueue<ResonanceBuyMessage> buyQueue;

    @Autowired
    ColaResonanceBiz biz;

    @Autowired
    ColaResonanceInviterRewardLogMapper rewardLogMapper;


    /**
     * 首页数据
     * @return
     */
    @IgnoreUserToken
    @RequestMapping("resonanceList")
    public AppResponse resonanceList(Integer processing){
        List<ResponseProjectListVo> result = biz.resonanceList(processing);
        return AppResponse.ok().data(result);
    }

    /**
     * 服务器时间,阶段,价格,数量等相关数据  (统计+推送)
     *      最新 3 个阶段价格,剩余,总共
     * @return
     */
    @IgnoreUserToken
    @RequestMapping("resonanceDetail")
    public AppResponse resonanceDetail(String coinCode){
        if(biz.getProjectServer(coinCode) == null){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        };
        Map<String,Object> result = biz.resonanceDetail(coinCode);
        return AppResponse.ok().data(result);
    }


    /**
     * 买入
     */
    @RequestMapping(value = "buy",method = RequestMethod.POST)
    public AppResponse buy(@RequestBody ResonanceBuyMessage params){
        if (params == null) return AppResponse.paramsError();
        String coinCode = params.getCoinCode();
        ResonanceProject project = biz.getProjectServer(coinCode);
        if(project == null){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        };
        if (StringUtils.isBlank(params.getPin()) || params.getAmount().compareTo(BigDecimal.ONE)<0)
            return AppResponse.paramsError();
        long currentTimeMillis = System.currentTimeMillis();
        if (biz.isEnd(coinCode)) return AppResponse.error(9881,ResponseCode.COIN_NOT_OPEN_MESSAGE);
        long startTimestamp = biz.getProjectStartTime(coinCode);
        long endTimestamp = biz.getProjectEndTime(coinCode);
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
        boolean success = biz.frozenBalance(params.getAmount(),project.symbol());
        if (!success) return AppResponse.error(ResponseCode.NO_ENOUGH_MONEY_CODE,ResponseCode.NO_ENOUGH_MONEY_MESSAGE);
        params.setCoinCode(project.coinCode());
        params.setSymbol(project.symbol());
        params.setUserId(BaseContextHandler.getUserID());
        buyQueue.offer(params);
        return AppResponse.ok();
    }


    /**
     * 排名 (统计+推送)
     */
    @IgnoreUserToken
    @RequestMapping("rank")
    public AppResponse rank(Integer limit,String coinCode){
        if(biz.getProjectServer(coinCode) == null){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        };
        if (limit == null) limit = 20;
        if (limit > 20) limit = 20;
        List<ColaResonanceRankResponse> result = biz.rank(limit,coinCode);
        return AppResponse.ok().data(result);
    }

    /**
     * 自己的资金情况
     */
    @RequestMapping("getLockNumber")
    public AppResponse getLockNumber(String coinCode){
        if(biz.getProjectServer(coinCode) == null){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        };
        String userID = BaseContextHandler.getUserID();
        Map<String,Object> result = biz.getLockNumber(userID,coinCode);
        return AppResponse.ok().data(result);
    }

    /**
     * 解锁情况
     */
    @RequestMapping("getUnlockDetail")
    public AppResponse getUnlockDetail(String coinCode){
        if(biz.getProjectServer(coinCode) == null){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        };
        String userID = BaseContextHandler.getUserID();
        List<ColaResonanceUnlockLog> result = biz.getUnlockDetail(userID,coinCode);
        return AppResponse.ok().data(result);
    }

    /**
     * 邀请奖励
     */
    @RequestMapping("inviterRewardLog")
    public AppResponse inviterRewardLog(String coinCode){
        if(biz.getProjectServer(coinCode) == null){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        };
        String userID = BaseContextHandler.getUserID();
        List<ColaResonanceInviterRewardLog> result = rewardLogMapper.inviterRewardLog(userID,coinCode);
        return AppResponse.ok().data(result);
    }


    /**
     * 最新共振 (统计+推送)
     */
    @IgnoreUserToken
    @RequestMapping("last")
    public AppResponse last(Integer limit,String coinCode){
        if(biz.getProjectServer(coinCode) == null){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        };
        if (limit == null) limit = 20;
        if (limit > 20) limit = 20;
        List<ColaResonanceLastResponse> result = biz.last(limit,coinCode);
        return AppResponse.ok().data(result);
    }



    /**
     * 实时计算当前购买的数量
     * @param amount
     * @return
     */
    @IgnoreUserToken
    @RequestMapping("calculate")
    public AppResponse calculate(BigDecimal amount,String coinCode){
        if(biz.getProjectServer(coinCode) == null){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        };
        if (amount.compareTo(BigDecimal.ZERO) <= 0) return AppResponse.paramsError();
        BigDecimal result = biz.calculate(amount,coinCode);
        return AppResponse.ok().data(result);
    }


    @IgnoreUserToken
    @RequestMapping("randomSomeRecord")
    public AppResponse randomSomeRecord(String key,String userId,BigDecimal amount,String coinCode){
        ResonanceProject project = biz.getProjectServer(coinCode);
        if(project == null){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        };
        if (!"kaiqiu".equals(key)){
            return AppResponse.error("");
        }
        long startTimestamp = biz.getProjectStartTime(coinCode);
        long endTimestamp = biz.getProjectEndTime(coinCode);
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis < startTimestamp || currentTimeMillis > endTimestamp ) {
            return AppResponse.error("暂未开放");
        }
        BigDecimal number = biz.calculate(amount,coinCode);
        ColaResonance iso = new ColaResonance();
        iso.setId(UUID.randomUUID().toString());
        iso.setCoinCode(project.coinCode());
        iso.setSymbol(project.symbol());
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
    public AppResponse dealDahu(String userId,BigDecimal amount,Integer round,String key,String coinCode){
        if(biz.getProjectServer(coinCode) == null){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        };
        if (!"kaiqiu".equals(key)){
            return AppResponse.error("");
        }
        String userID = BaseContextHandler.getUserID();
        Integer id = Integer.valueOf(userID);
        if (id >= 100000) return AppResponse.error(ColaLanguage.FORBIDDEN);
        Map<String,Object> result = biz.dealDahu(userId,amount,round,coinCode);
        return AppResponse.ok().data(result);
    }


    /**
     * 立即解锁 1 %
     * @return
     */
    @RequestMapping("unlockPercent")
    public AppResponse unlockPercent(String userId,String key,Integer day,String coinCode){
        if(biz.getProjectServer(coinCode) == null){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        };
        if (!"kaiqiu".equals(key)){
            return AppResponse.error("");
        }
        if (day == null) day = 1;
        String userID = BaseContextHandler.getUserID();
        Integer id = Integer.valueOf(userID);
        if (id >= 100000) return AppResponse.error(ColaLanguage.FORBIDDEN);
        List<Map<String,Object>> result = biz.unlockPercent(userId,day,coinCode);
        return AppResponse.ok().data(result);
    }




}
