package com.bitcola.exchange.launchpad.service;

import com.bitcola.exchange.launchpad.biz.ColaLaunchpadProjectBiz;
import com.bitcola.exchange.launchpad.config.DelayQueueBySpeed;
import com.bitcola.exchange.launchpad.config.DelayQueueBySpeedMap;
import com.bitcola.exchange.launchpad.dto.ColaUserStatus;
import com.bitcola.exchange.launchpad.entity.ColaLaunchpadProjectIeo;
import com.bitcola.exchange.launchpad.feign.IExchangeFeign;
import com.bitcola.exchange.launchpad.mapper.ColaLaunchpadProjectIeoMapper;
import com.bitcola.exchange.launchpad.message.BuyMessage;
import com.bitcola.exchange.launchpad.message.BuyResponse;
import com.bitcola.exchange.launchpad.message.ClearMessage;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.msg.AppResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zkq
 * @create 2019-03-15 15:54
 **/
@Service
public class BuyService implements ApplicationRunner {
    public static final Map<String, BuyResponse> RESPONSE = new ConcurrentHashMap<>();
    public static final Map<String,Map<String,BigDecimal>> buyNumberCache = new ConcurrentHashMap<>();
    @Autowired
    DelayQueueBySpeed<BuyMessage> buyQueue;
    @Autowired
    ColaLaunchpadProjectBiz biz;
    @Autowired
    ColaLaunchpadProjectIeoMapper ieoMapper;
    @Autowired
    IExchangeFeign exchangeFeign;
    @Autowired
    DelayQueueBySpeedMap<ClearMessage> queueMap;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        //new Thread(() -> {
        //    while (true){
        //        try {
        //            List<BuyMessage> message = buyQueue.getMessage();
        //            if (message.size()>0){
        //                dealMessage(message);
        //            }
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //        }
        //    }
        //}).start();
    }

    private void dealMessage(List<BuyMessage> message) {
        List<String> userIds = new ArrayList<>();
        List<String> projectIds = new ArrayList<>();
        for (BuyMessage buyMessage : message) {
            userIds.add(buyMessage.getUserId());
            projectIds.add(buyMessage.getParams().getId());
        }
        // 查询这里 id 有没有 pin 和 kyc
        List<ColaUserStatus> users = biz.getUserPinAndKycByIds(userIds);
        Map<String,ColaUserStatus> userStatusMap = new HashMap<>();
        for (ColaUserStatus map : users) {
            userStatusMap.put(map.getId(),map);
        }
        List<ColaLaunchpadProjectIeo> projects = ieoMapper.selectProjectByIds(projectIds);
        Map<String,ColaLaunchpadProjectIeo> projectStatusMap = new HashMap<>();
        for (ColaLaunchpadProjectIeo project : projects) {
            projectStatusMap.put(project.getId(),project);
        }

        List<BuyMessage> pass = new ArrayList<>();
        for (BuyMessage buyMessage : message) {
            boolean check = check(buyMessage, userStatusMap, projectStatusMap.get(buyMessage.getParams().getId()));
            if (check){
                pass.add(buyMessage);
            } else {
                unLock(buyMessage.getLock());
            }
        }
        for (BuyMessage buyMessage : pass) {
            buy(buyMessage,projectStatusMap.get(buyMessage.getParams().getId()));
            unLock(buyMessage.getLock());
        }


    }

    private void buy(BuyMessage buyMessage,ColaLaunchpadProjectIeo ieo) {
        BigDecimal price = ieo.getPrice().divide(getPrice(buyMessage.getParams().getSymbol()),10, RoundingMode.HALF_DOWN);
        BigDecimal frozenNumber = price.multiply(buyMessage.getParams().getNumber());
        BigDecimal remain = ClearService.REMAIN.get(buyMessage.getParams().getId());
        if (remain == null || remain.compareTo(BigDecimal.ZERO)<=0){
            RESPONSE.put(buyMessage.getId(),new BuyResponse(false,null,
                    ResponseCode.TIP_ERROR_CODE, ColaLanguage.LAUNCHPAD_OVER));
            return;
        }

        int j = biz.frozenUserBalance(buyMessage.getUserId(),buyMessage.getParams().getSymbol(),frozenNumber);
        if (j != 1){
            RESPONSE.put(buyMessage.getId(),new BuyResponse(false,null,
                    ResponseCode.NO_ENOUGH_MONEY_CODE, ResponseCode.NO_ENOUGH_MONEY_MESSAGE));
            return;
        }
        BigDecimal buyRealNumber = buyMessage.getParams().getNumber();
        if (remain.compareTo(buyMessage.getParams().getNumber())<0){
            buyRealNumber = remain;
        }
        remain = remain.subtract(buyRealNumber);
        BigDecimal reward = BigDecimal.ZERO;
        if (ieo.getReward().compareTo(BigDecimal.ZERO)>=0){
            reward = buyRealNumber.multiply(ieo.getReward());
            reward = remain.min(reward);
            remain = remain.subtract(reward);
        }
        ClearService.REMAIN.put(buyMessage.getParams().getId(),remain);
        // 生成清算消息
        queueMap.putMessage(buyMessage.getParams().getId(), new ClearMessage(buyMessage.getParams().getId(),buyMessage.getUserId(),ieo.getUserId(),ieo.getCoinCode(),buyMessage.getParams().getSymbol(),
                price,buyMessage.getParams().getNumber(),buyRealNumber,System.currentTimeMillis(),reward));
        RESPONSE.put(buyMessage.getId(),new BuyResponse(true,buyRealNumber,0,null));
        Map<String, BigDecimal> map = buyNumberCache.get(ieo.getId());
        if(map == null){
            map = new HashMap<>();
            buyNumberCache.put(ieo.getId(),map);
        }
        BigDecimal number = map.get(buyMessage.getUserId());
        if (number == null){
            number = BigDecimal.ZERO;
        }
        map.put(buyMessage.getUserId(),number.add(buyRealNumber));
    }

    /**
     * 检查交易是否合理
     * @return
     */
    private boolean check(BuyMessage buyMessage,Map<String,ColaUserStatus> userStatusMap,ColaLaunchpadProjectIeo ieo){
        String userId = buyMessage.getUserId();
        if (ieo == null || System.currentTimeMillis() < ieo.getStart() || System.currentTimeMillis() > ieo.getEnd()) {
            RESPONSE.put(buyMessage.getId(),new BuyResponse(false,null,
                    ResponseCode.TIP_ERROR_CODE, ColaLanguage.FORBIDDEN));
            return false;
        }
        if (buyMessage.getParams().getNumber().compareTo(ieo.getAllowMinNumber())<0 || buyMessage.getParams().getNumber().compareTo(ieo.getAllowMaxNumber())>0){
            RESPONSE.put(buyMessage.getId(),new BuyResponse(false,null,
                    ResponseCode.TIP_ERROR_CODE, ColaLanguage.FORBIDDEN));
            return false;
        }
        // 是否满足最大限额
        Map<String, BigDecimal> map = buyNumberCache.get(ieo.getId());
        if(map != null){
            BigDecimal buyNumber = map.get(userId);
            if (buyNumber.add(buyMessage.getParams().getNumber()).compareTo(ieo.getAllowTotalNumber())>0){
                RESPONSE.put(buyMessage.getId(),new BuyResponse(false,null,
                        ResponseCode.TIP_ERROR_CODE, ColaLanguage.LAUNCHPAD_BUY_MAX_LIMIT));
                return false;
            }
        }

        if (StringUtils.isBlank(userStatusMap.get(userId).getPin())){
            RESPONSE.put(buyMessage.getId(),new BuyResponse(false,null,
                    ResponseCode.NO_MONEY_PASSWORD_CODE, ResponseCode.NO_MONEY_PASSWORD_MESSAGE));
            return false;
        }
        if (1 != userStatusMap.get(userId).getKyc()){
            RESPONSE.put(buyMessage.getId(),new BuyResponse(false,null,
                    ResponseCode.NOT_PASS_KYC_CODE, ResponseCode.NOT_PASS_KYC_MESSAGE));
            return false;
        }
        if (!EncoderUtil.matches(buyMessage.getParams().getPin(),userStatusMap.get(userId).getPin())){
            RESPONSE.put(buyMessage.getId(),new BuyResponse(false,null,
                    ResponseCode.PIN_ERROR_CODE, ResponseCode.PIN_ERROR_MESSAGE));
            return false;
        }
        if (ieo.getStatus() == 2) {
            RESPONSE.put(buyMessage.getId(),new BuyResponse(false,null,
                    ResponseCode.TIP_ERROR_CODE, ColaLanguage.LAUNCHPAD_OVER));
            return false;
        }
        return true;
    }


    BigDecimal btcPrice;
    BigDecimal ethPrice;
    String USDT = "USDT";
    String BTC = "BTC";
    String ETH = "ETH";

    private BigDecimal getPrice(String symbol){
        if (symbol.equals(USDT)) return BigDecimal.ONE;
        BigDecimal symbolPrice = null;
        try {
            AppResponse usdPriceResponse = exchangeFeign.getUsdPrice(symbol);
            symbolPrice = new BigDecimal(usdPriceResponse.getData().toString());
            if (BTC.equals(symbol)){
                btcPrice = symbolPrice;
            } else if (ETH.equals(symbol)){
                ethPrice = symbolPrice;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (symbolPrice == null){
            if (BTC.equals(symbol) || btcPrice != null){
                return btcPrice;
            } else if (ETH.equals(symbol) || ethPrice != null){
                return ethPrice;
            } else {
                throw new RuntimeException("交易未准备就绪");
            }
        } else {
            return symbolPrice;
        }
    }



    private void unLock(Object lock){
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}
