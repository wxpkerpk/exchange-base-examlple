package com.bitcola.exchange.script.service;

import com.bitcola.exchange.constant.OrderDirection;
import com.bitcola.exchange.entity.OrderBook;
import com.bitcola.exchange.mapper.ScriptMapper;
import com.bitcola.exchange.message.OrderMessage;
import com.bitcola.exchange.script.ScriptUser;
import com.bitcola.exchange.script.data.PairScale;
import com.bitcola.exchange.script.exception.MakeOrderException;
import com.bitcola.exchange.script.params.AutoMakeOrderParams;
import com.bitcola.exchange.script.params.BalanceCoinPriceParams;
import com.bitcola.exchange.script.params.DynamicDepthParams;
import com.bitcola.exchange.script.queue.ScriptDelayQueue;
import com.bitcola.exchange.script.util.PlatformPriceUtil;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.service.KlineService;
import com.bitcola.exchange.service.MatchService;
import com.bitcola.exchange.script.util.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zkq
 * @create 2019-03-18 16:35
 **/
@Service
public class ScriptService {
    public static final String autoMakeOrder = "autoMakeOrder";
    public static final String dynamicDepth = "dynamicDepth";
    public static final String balanceCoinPrice = "balanceCoinPrice";

    Timer autoMakeOrderTimer = new Timer();
    TimerTask autoMakeOrderTimerTask = new TimerTask() {
        @Override
        public void run() {}
    };
    Timer dynamicDepthTimer = new Timer();
    TimerTask dynamicDepthTimerTask = new TimerTask() {
        @Override
        public void run() {}
    };
    Timer balanceCoinPriceTimer = new Timer();
    TimerTask balanceCoinPriceTimerTask = new TimerTask() {
        @Override
        public void run() {}
    };

    @Resource(name = "autoMakeOrderQueue")
    ScriptDelayQueue<AutoMakeOrderParams> autoMakeOrderQueue;

    @Resource(name = "autoDynamicDepthQueue")
    ScriptDelayQueue<DynamicDepthParams> autoDynamicDepthQueue;

    @Autowired
    ScriptMapper mapper;

    @Autowired
    KlineService klineService;

    @Autowired
    QueueService service;

    public void startAutoMakeOrderTimer(List<AutoMakeOrderParams> params){

        stopScript(autoMakeOrder);

        autoMakeOrderTimer = new Timer();
        autoMakeOrderTimerTask = new TimerTask() {
            @Override
            public void run() {
            for (AutoMakeOrderParams param : params) {
                autoMakeOrderQueue.putMessage(param,param.getPerHourTime(), TimeUnit.HOURS);
            }
            }
        };
        autoMakeOrderTimer.schedule(autoMakeOrderTimerTask,0,1000 * 60 * 60 * 24);
    }

    /**
     * 缓存交易对精度
     */
    public void cachePairScale(){
        List<PairScale> result = mapper.getPairScale();
        for (PairScale scale : result) {
            String pair = scale.getPair();
            QueueService.scaleCache.put(pair,scale);
        }
    }


    public void startDynamicDepthTimer(List<DynamicDepthParams> params) {
        stopScript(dynamicDepth);
        dynamicDepthTimer = new Timer();
        dynamicDepthTimerTask = new TimerTask() {
            @Override
            public void run() {
            for (DynamicDepthParams param : params) {
                autoDynamicDepthQueue.putMessage(param,param.getPerMinTime(),TimeUnit.MINUTES);
            }
            }
        };
        dynamicDepthTimer.schedule(dynamicDepthTimerTask,0,1000 * 60 * 60 * 24);
    }

    public void startBalanceCoinPrice(List<BalanceCoinPriceParams> params) {
        stopScript(balanceCoinPrice);
        balanceCoinPriceTimer = new Timer();
        balanceCoinPriceTimerTask = new TimerTask() {
            @Override
            public void run() {
                service.doBalanceCoinPrice(params);
            }
        };
        balanceCoinPriceTimer.schedule(balanceCoinPriceTimerTask,0,1000 * 10);
        QueueService.statusCache.put(balanceCoinPrice,true);
    }


    public void stopScript(String script) {
        if (balanceCoinPrice.equalsIgnoreCase(script)){
            balanceCoinPriceTimer.cancel();
            balanceCoinPriceTimerTask.cancel();
            QueueService.statusCache.put(balanceCoinPrice,false);
        } else if (dynamicDepth.equalsIgnoreCase(script)){
            dynamicDepthTimer.cancel();
            dynamicDepthTimerTask.cancel();
            autoDynamicDepthQueue.clear();
            QueueService.statusCache.put(dynamicDepth,false);
        } else if (autoMakeOrder.equalsIgnoreCase(script)){
            autoMakeOrderTimer.cancel();
            autoMakeOrderTimerTask.cancel();
            autoMakeOrderQueue.clear();
            QueueService.statusCache.put(autoMakeOrder,false);
        } else if ("all".equalsIgnoreCase(script)){
            stopScript(balanceCoinPrice);
            stopScript(dynamicDepth);
            stopScript(autoMakeOrder);
        }
    }

}

