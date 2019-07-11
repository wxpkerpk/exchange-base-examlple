package com.bitcola.exchange.script;

import ch.qos.logback.core.util.TimeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bitcola.exchange.biz.ColaExchangeBiz;
import com.bitcola.exchange.constant.OrderDirection;
import com.bitcola.exchange.data.MakeOrderParams;
import com.bitcola.exchange.mapper.ScriptMapper;
import com.bitcola.exchange.rest.ColaExchangeController;
import com.bitcola.exchange.script.data.Config;
import com.bitcola.exchange.script.data.PairScale;
import com.bitcola.exchange.script.params.AutoMakeOrderParams;
import com.bitcola.exchange.script.params.BalanceCoinPriceParams;
import com.bitcola.exchange.script.params.DynamicDepthParams;
import com.bitcola.exchange.script.params.InitOrder;
import com.bitcola.exchange.script.queue.ScriptDelayQueue;
import com.bitcola.exchange.script.service.QueueService;
import com.bitcola.exchange.script.service.ScriptService;
import com.bitcola.exchange.script.util.RandomUtil;
import com.bitcola.exchange.script.vo.BalanceDetail;
import com.bitcola.exchange.script.vo.ScriptBalance;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.service.MatchService;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

/**
 *
 * BITCOLA价格平衡机器人
 * 1.自动交易
 * 1.1 根据当前买卖价格，在卖1和买1价格之间的随机价格，产生交易记录。需保证交易百分百完成。可以从随机数设计上来避免这个问题。如果未成交完则取消也可避免不完全成交这个问题
 * 买卖方向：随机
 * 价格：非特殊情况下买卖之间的随机价格。
 * 价格特殊情况：买一和卖一价格相差最小精度。此种情况需要考虑（因为此时用户可以通过此种方式强制机器人买入或者卖出，以此完成买入或卖出）建议此种情况下不进行操作
 * 交易对：可配置
 * 数量范围：可配置
 * 频率：可配置
 *
 * 1.2 自动生成深度
 * 1.2.1 根据当前选择交易对，设置当前币种价格。然后从当前价格自动生成卖单，卖单，买单和卖单价格差使用递增队列，深度买卖双方下单数量可配置。
 *
 * 2.价格平衡
 * 2.1 主流币价格平衡算法，根据其他平台主流币价格来对我们平台价格进行平衡。
 *
 * 价格参考：火币、币安等主流网站的价格。使用多个交易所价格。可配置
 * 交易对：可配置
 * 平衡范围：可配置，（如当我们平台卖单价格小于其他平台成交价5%时，我们将对价格进行平衡）
 * 平衡方式：当我们平台卖单价格低于其他平台成交价时，吃掉比其它平台低的订单，并增加买单订单。
 * 参数，nowPrice(当前从其它平台获取的价格)，depth(当前交易对深度)，percent(当前交易对价格阈值，)
 * 例如。 nowPrice =10$.
 * 假如depth中买单价格大于 10*(1+percent)，则将当前深度中大于此部分的买单吃掉。
 * 假如depth中卖单价格小于 10*（1-percent）,则将当前深度中小于此部分价格的卖单吃掉
 *
 * 当吃掉n个买单之后，卖出订单价格也需要随之下降。机器人此时应该，以当前最新价格为基准，按照一定的价格增量。增加n个卖单
 * 例如，nowPrice=10$,吃掉11，12两个买单，则此时可以增加10.3，10.45的价格的订单，此处对应的数量可配置，一般不会很大。在吃掉n个买单后，也需要在最新价格的下方，增加n个买单。以保证深度列表的数据平衡
 *
 * 当触发我们设置的差价范围后，（采用多平台价格加权平均），
 * 3.动态深度
 * 3.1 为了让整个平台内的深度动起来，在配置的交易对买卖深度中随机进行进行挂单并取消
 * 交易对：可配置
 * 数量、价格范围：可配置
 * 需求：下单之后立马取消
 * 频率：可配置
 * 深度范围：一般来说采取随机第4、5、6条深度。
 *
 *
 *
 *
 *
 *
 *
 * @author zkq
 * @create 2019-02-21 10:20
 **/
@Log4j2
@RestController
@RequestMapping("script")
public class ColaScriptController {



    @Autowired
    QueueService queueService;

    @Autowired
    ScriptService service;

    @Autowired
    ScriptMapper mapper;

    @RequestMapping(value = "initOrder",method = RequestMethod.POST)
    public AppResponse initOrder(@RequestBody InitOrder order){
        String userID = BaseContextHandler.getUserID();
        if (!ScriptUser.SCRIPT_USER.contains(userID)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        service.cachePairScale();
        BaseContextHandler.setUserID(ScriptUser.BALANCE_PRICE_USER);
        // 初始化
        Integer size = order.getSize();
        BigDecimal buyPrice = order.getCurrentPrice();
        BigDecimal sellPrice = order.getCurrentPrice();
        for (int i = 0; i < size; i++) {
            // 补买单
            BigDecimal randomRate = RandomUtil.getRandom(order.getMinRate(), order.getMaxRate(),8,8);
            if (randomRate == null) randomRate = order.getMaxRate();
            buyPrice = buyPrice.multiply(BigDecimal.ONE.subtract(randomRate));
            BigDecimal number = RandomUtil.getRandom(order.getMinNumber(),order.getMaxNumber(),8,8);
            queueService.makeOrder(order.getPair(),buyPrice,number, OrderDirection.BUY);
        }
        for (int i = 0; i < size; i++) {
            // 补卖单
            BigDecimal randomRate = RandomUtil.getRandom(order.getMinRate(), order.getMaxRate(),8,8);
            if (randomRate == null) randomRate = order.getMaxRate();
            sellPrice = sellPrice.multiply(BigDecimal.ONE.add(randomRate));
            BigDecimal number = RandomUtil.getRandom(order.getMinNumber(),order.getMaxNumber(),8,8);
            queueService.makeOrder(order.getPair(),sellPrice,number, OrderDirection.SELL);
        }
        return AppResponse.ok();
    }


    /**
     * 自动交易
     *
     * pair : 每个交易对单独配置
     * maxNumber : 最小精度 ~ maxNumber 之间随机成交数量
     * perMinTime : 1分钟内随机时间成交 perMinTime 次
     * 价格取买1到卖1之间随机
     *
     * @return
     */
    @RequestMapping(value = "autoMakeOrder",method = RequestMethod.POST)
    public AppResponse autoMakeOrder(@RequestBody List<AutoMakeOrderParams> params){
        String userID = BaseContextHandler.getUserID();
        if (!ScriptUser.SCRIPT_USER.contains(userID)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        Config config = new Config();
        config.setScript(ScriptService.autoMakeOrder);
        config.setConfig(JSONObject.toJSONString(params));
        mapper.updateByPrimaryKey(config);
        service.cachePairScale();
        service.startAutoMakeOrderTimer(params);
        queueService.startAutoMakeOrderQueue();
        return AppResponse.ok();
    }

    /**
     * 动态深度,随机频率
     *      不产生订单
     *      不记录日志
     *   创建特殊订单,特殊订单不撮合,如果其他订单需要撮合此订单,则此订单立即取消
     */
    @RequestMapping(value = "dynamicDepth",method = RequestMethod.POST)
    public AppResponse dynamicDepth(@RequestBody List<DynamicDepthParams> params){
        String userID = BaseContextHandler.getUserID();
        if (!ScriptUser.SCRIPT_USER.contains(userID)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        Config config = new Config();
        config.setScript(ScriptService.dynamicDepth);
        config.setConfig(JSONObject.toJSONString(params));
        mapper.updateByPrimaryKey(config);
        service.cachePairScale();
        service.startDynamicDepthTimer(params);
        queueService.startDynamicDepthQueue();
        return AppResponse.ok();
    }



    @RequestMapping(value = "balanceCoinPrice",method = RequestMethod.POST)
    public AppResponse balanceCoinPrice(@RequestBody List<BalanceCoinPriceParams> params){
        String userID = BaseContextHandler.getUserID();
        if (!ScriptUser.SCRIPT_USER.contains(userID)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        Config config = new Config();
        config.setScript(ScriptService.balanceCoinPrice);
        config.setConfig(JSONObject.toJSONString(params));
        mapper.updateByPrimaryKey(config);
        service.cachePairScale();
        service.startBalanceCoinPrice(params);
        return AppResponse.ok();
    }

    @RequestMapping("status")
    public AppResponse status(){
        String userID = BaseContextHandler.getUserID();
        if (!ScriptUser.SCRIPT_USER.contains(userID)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        return AppResponse.ok().data(QueueService.statusCache);
    }


    @RequestMapping("stopScript")
    public AppResponse stopScript(String script){
        String userID = BaseContextHandler.getUserID();
        if (!ScriptUser.SCRIPT_USER.contains(userID)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        service.stopScript(script);
        return AppResponse.ok();
    }


    @RequestMapping("getConfig")
    public AppResponse getConfig(String script){
        String userID = BaseContextHandler.getUserID();
        if (!ScriptUser.SCRIPT_USER.contains(userID)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        Object data = null;
        List<PairScale> result = mapper.getPairScale();
        if (ScriptService.balanceCoinPrice.equalsIgnoreCase(script)){
            data = getBalanceCoinPriceConfig(result);
        } else if (ScriptService.dynamicDepth.equalsIgnoreCase(script)){
            data = getDynamicDepthConfig(result);
        } else if (ScriptService.autoMakeOrder.equalsIgnoreCase(script)){
            data = getAutoMakeOrderConfig(result);
        } else if ("all".equalsIgnoreCase(script)){
            Map<String,List> map = new HashMap<>();
            map.put(ScriptService.balanceCoinPrice,getBalanceCoinPriceConfig(result));
            map.put(ScriptService.dynamicDepth,getDynamicDepthConfig(result));
            map.put(ScriptService.autoMakeOrder,getAutoMakeOrderConfig(result));
            data = map;
        }
        return AppResponse.ok().data(data);
    }

    public static void main(String[] args) {
        DynamicDepthParams params = new DynamicDepthParams();
        params.setPair("ETH_USDT");
        params.setMaxNumber(BigDecimal.ZERO);
        params.setMinNumber(BigDecimal.ZERO);
        System.out.println(JSONObject.toJSONString(params));
    }

    @Autowired
    ColaExchangeBiz exchangeBiz;

    @RequestMapping("getBalance")
    public AppResponse getBalance(){
        String userID = BaseContextHandler.getUserID();
        if (!ScriptUser.SCRIPT_USER.contains(userID)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        List<BalanceDetail> makeOrder = mapper.getBalance(ScriptUser.AUTO_MAKE_ORDER_USER);
        List<BalanceDetail> balance = mapper.getBalance(ScriptUser.BALANCE_PRICE_USER);
        Map<String,BalanceDetail> map = new HashMap<>();
        BigDecimal aTotal = BigDecimal.ZERO;
        BigDecimal bTotal = BigDecimal.ZERO;
        for (BalanceDetail item : makeOrder) {
            BalanceDetail detail = map.computeIfAbsent(item.getCoinCode(), k -> new BalanceDetail());
            detail.setCoinCode(item.getCoinCode());
            detail.setAAvailable(item.getAAvailable());
            detail.setAFrozen(item.getAFrozen());
            detail.setATotal(getProfitAndLoss(detail.getCoinCode(),item.getAAvailable().add(item.getAFrozen())));
            detail.setAWorth(detail.getATotal().multiply(exchangeBiz.getCoinPrice(item.getCoinCode())));
            aTotal = aTotal.add(detail.getAWorth());
        }
        for (BalanceDetail item : balance) {
            BalanceDetail detail = map.computeIfAbsent(item.getCoinCode(), k -> new BalanceDetail());
            detail.setCoinCode(item.getCoinCode());
            detail.setBAvailable(item.getAAvailable());
            detail.setBFrozen(item.getAFrozen());
            detail.setBTotal(getProfitAndLoss(detail.getCoinCode(),item.getAAvailable().add(item.getAFrozen())));
            detail.setBWorth(detail.getBTotal().multiply(exchangeBiz.getCoinPrice(item.getCoinCode())));
            bTotal = bTotal.add(detail.getBWorth());
        }
        ScriptBalance scriptBalance = new ScriptBalance();
        scriptBalance.setATotal(aTotal);
        scriptBalance.setBTotal(bTotal);
        scriptBalance.setTotal(scriptBalance.getATotal().add(scriptBalance.getBTotal()));
        scriptBalance.setBalanceDetail(new ArrayList<>(map.values()));
        return AppResponse.ok().data(scriptBalance);
    }

    private static BigDecimal getProfitAndLoss(String coinCode,BigDecimal total){
        if (total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        if ("COLA".equals(coinCode)){
            return total.subtract(new BigDecimal("10000000"));
        } else {
            return total.subtract(new BigDecimal("500000000"));
        }
    }



    private List getBalanceCoinPriceConfig(List<PairScale> result){
        Config config = mapper.selectByPrimaryKey(ScriptService.balanceCoinPrice);
        if (config != null){
            return JSONObject.parseArray(config.getConfig(), BalanceCoinPriceParams.class);
        }
        List<BalanceCoinPriceParams> data = new ArrayList<>();
        for (PairScale pairScale : result) {
            BalanceCoinPriceParams params = new BalanceCoinPriceParams();
            params.setPair(pairScale.getPair());
            params.setSafeNumber(RandomUtil.getMinDecimal(pairScale.getAmountScale()).multiply(new BigDecimal(1000)));
            data.add(params);
        }
        config = new Config();
        config.setScript(ScriptService.balanceCoinPrice);
        config.setConfig(JSONObject.toJSONString(data));
        mapper.insert(config);
        return data;
    }
    private List getDynamicDepthConfig(List<PairScale> result){
        Config config = mapper.selectByPrimaryKey(ScriptService.dynamicDepth);
        if (config != null){
            return JSONObject.parseArray(config.getConfig(), DynamicDepthParams.class);
        }
        List<DynamicDepthParams> data = new ArrayList<>();
        for (PairScale pairScale : result) {
            DynamicDepthParams params = new DynamicDepthParams();
            params.setPair(pairScale.getPair());
            params.setMinNumber(RandomUtil.getMinDecimal(pairScale.getAmountScale()));
            params.setMaxNumber(params.getMinNumber().multiply(new BigDecimal(100)));
            data.add(params);
        }
        config = new Config();
        config.setScript(ScriptService.dynamicDepth);
        config.setConfig(JSONObject.toJSONString(data));
        mapper.insert(config);
        return data;
    }
    private List getAutoMakeOrderConfig(List<PairScale> result){
        Config config = mapper.selectByPrimaryKey(ScriptService.autoMakeOrder);
        if (config != null){
            return JSONObject.parseArray(config.getConfig(), AutoMakeOrderParams.class);
        }
        List<AutoMakeOrderParams> data = new ArrayList<>();
        for (PairScale pairScale : result) {
            AutoMakeOrderParams params = new AutoMakeOrderParams();
            params.setPair(pairScale.getPair());
            params.setMinNumber(RandomUtil.getMinDecimal(pairScale.getAmountScale()));
            params.setMaxNumber(params.getMinNumber().multiply(new BigDecimal(100)));
            data.add(params);
        }
        config = new Config();
        config.setScript(ScriptService.autoMakeOrder);
        config.setConfig(JSONObject.toJSONString(data));
        mapper.insert(config);
        return data;
    }







}
