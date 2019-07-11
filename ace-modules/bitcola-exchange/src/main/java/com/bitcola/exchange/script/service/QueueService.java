package com.bitcola.exchange.script.service;

import com.bitcola.exchange.biz.ColaExchangeBiz;
import com.bitcola.exchange.constant.OrderDirection;
import com.bitcola.exchange.constant.OrderStatus;
import com.bitcola.exchange.constant.OrderType;
import com.bitcola.exchange.entity.OrderBook;
import com.bitcola.exchange.message.OrderMessage;
import com.bitcola.exchange.queue.LinkedBlockingQueueMap;
import com.bitcola.exchange.rest.ColaExchangeController;
import com.bitcola.exchange.script.ScriptUser;
import com.bitcola.exchange.script.data.MinChange;
import com.bitcola.exchange.script.exception.MakeOrderException;
import com.bitcola.exchange.script.data.PairScale;
import com.bitcola.exchange.script.exception.MakeOrderNoMoneyException;
import com.bitcola.exchange.script.params.AutoMakeOrderParams;
import com.bitcola.exchange.script.params.BalanceCoinPriceParams;
import com.bitcola.exchange.script.params.DynamicDepthParams;
import com.bitcola.exchange.script.queue.DelayMessage;
import com.bitcola.exchange.script.queue.ScriptDelayQueue;
import com.bitcola.exchange.script.util.PlatformPriceUtil;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.service.KlineService;
import com.bitcola.exchange.service.MatchService;
import com.bitcola.exchange.script.util.RandomUtil;
import com.bitcola.exchange.util.Snowflake;
import lombok.extern.log4j.Log4j2;
import net.sf.jsqlparser.util.deparser.OrderByDeParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zkq
 * @create 2019-03-18 17:46
 **/
@Log4j2
@Service
public class QueueService {

    private static final BigDecimal ONE_POINT_FIVE = new BigDecimal("1.5");
    private static final BigDecimal POINT_ONE = new BigDecimal("0.1");
    private static final BigDecimal POINT_TWO = new BigDecimal("0.2");
    private static final BigDecimal TWO = new BigDecimal(2);
    private static final BigDecimal THREE = new BigDecimal(3);
    private static final BigDecimal FIVE = new BigDecimal(5);

    public static final Map<String, PairScale> scaleCache = new ConcurrentHashMap<>();
    public static final Map<String, BigDecimal> platformPriceCache = new ConcurrentHashMap<>();
    public static final Map<String, MinChange> minChangeCache = new ConcurrentHashMap<>();
    public static final Map<String, Boolean> statusCache = new ConcurrentHashMap<>();
    static {
        statusCache.put(ScriptService.balanceCoinPrice,false);
        statusCache.put(ScriptService.dynamicDepth,false);
        statusCache.put(ScriptService.autoMakeOrder,false);

    }
    private static final String USDT = "USDT";
    private Snowflake snowflake = new Snowflake();

    @Resource(name = "autoMakeOrderQueue")
    ScriptDelayQueue<AutoMakeOrderParams> autoMakeOrderQueue;

    @Resource(name = "matchOrderQueue")
    LinkedBlockingQueueMap<OrderMessage> matchOrderQueue;


    @Resource(name = "autoDynamicDepthQueue")
    ScriptDelayQueue<DynamicDepthParams> autoDynamicDepthQueue;

    @Autowired
    ColaExchangeBiz biz;

    @Autowired
    ScriptService service;

    @Autowired
    KlineService klineService;

    ExecutorService executorService = Executors.newFixedThreadPool(100);


    public void startAutoMakeOrderQueue(){
        if (!statusCache.get(ScriptService.autoMakeOrder)){
            statusCache.put(ScriptService.autoMakeOrder,true);
            new Thread(()->{
                try {
                    while (statusCache.get(ScriptService.autoMakeOrder)){
                        DelayMessage<AutoMakeOrderParams> message = autoMakeOrderQueue.getMessage();
                        if (message != null){
                            dealAutoMakeOrder(message.getData());
                        }
                    }
                } catch (Exception e) {
                    service.stopScript(ScriptService.autoMakeOrder);
                    e.printStackTrace();
                }
            }).start();
        }
    }
    public void startDynamicDepthQueue(){
        if (!statusCache.get(ScriptService.dynamicDepth)){
            statusCache.put(ScriptService.dynamicDepth,true);
            new Thread(()->{
                try {
                    while (statusCache.get(ScriptService.dynamicDepth)){
                        DelayMessage<DynamicDepthParams> message = autoDynamicDepthQueue.getMessage();
                        if (message != null){
                            dealDynamicDepth(message.getData());
                        }
                    }
                } catch (Exception e) {
                    service.stopScript(ScriptService.dynamicDepth);
                    e.printStackTrace();
                }
            }).start();
        }
    }
    public void doBalanceCoinPrice(List<BalanceCoinPriceParams> params) {
        try {
            Map<String,BigDecimal> binancePriceMap = PlatformPriceUtil.getBinancePrice();
            Map<String,BigDecimal> gateioPriceMap = PlatformPriceUtil.getGateioPrice();
            Map<String,BigDecimal> huobiPriceMap = PlatformPriceUtil.getHuobiPrice();

            for (BalanceCoinPriceParams param : params) {
                BigDecimal currentPrice = doBalanceCoinPriceByPair(param,binancePriceMap,gateioPriceMap,huobiPriceMap);
            }
        } catch (MakeOrderException e) {
            service.stopScript(ScriptService.balanceCoinPrice);
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }


    }


    /**
     * 深度动态变化
     * @param data
     */
    private void dealDynamicDepth(DynamicDepthParams data) {
        String pair = data.getPair();
        Integer amountScale = scaleCache.get(pair).getAmountScale();
        Integer priceScale = scaleCache.get(pair).getPriceScale();
        List<BigDecimal[]> sellDepth = MatchService.sellDepth.get(pair).getDepth(10, priceScale);
        if (sellDepth.size() == 10){
            BigDecimal randomMaxPrice = RandomUtil.getRandom(sellDepth.get(1)[0], sellDepth.get(7)[0], priceScale, priceScale);
            BigDecimal sellPrice = RandomUtil.getRandom(sellDepth.get(1)[0], randomMaxPrice, priceScale, priceScale);
            BigDecimal sellNumber = RandomUtil.getRandom(data.getMinNumber(), data.getMaxNumber(), amountScale - 3, amountScale);
            executorService.submit(() ->{
                makeOrderAndCancel(pair,sellPrice,sellNumber,OrderDirection.SELL);
            });
        }
        List<BigDecimal[]> buyDepth = MatchService.buyDepth.get(pair).getDepth(10, priceScale);
        if (buyDepth.size() == 10){
            BigDecimal randomMinPrice = RandomUtil.getRandom(buyDepth.get(7)[0],buyDepth.get(1)[0], priceScale, priceScale);
            BigDecimal buyPrice = RandomUtil.getRandom(randomMinPrice, buyDepth.get(1)[0], priceScale, priceScale);
            BigDecimal buyNumber = RandomUtil.getRandom(data.getMinNumber(), data.getMaxNumber(), amountScale - 3, amountScale);
            executorService.submit(() ->{
                try {
                    Thread.sleep(new Random().nextInt(1000)+1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                makeOrderAndCancel(pair,buyPrice,buyNumber,OrderDirection.BUY);
            });
        }



    }


    /**
     * 自动交易,产生市场订单
     * @param data
     */
    private void dealAutoMakeOrder(AutoMakeOrderParams data){
        BaseContextHandler.setUserID(ScriptUser.AUTO_MAKE_ORDER_USER);
        String pair = data.getPair();
        Integer amountScale = scaleCache.get(data.getPair()).getAmountScale();
        Integer priceScale = scaleCache.get(data.getPair()).getPriceScale();
        BigDecimal marketPrice = klineService.getMarketPrice(pair);
        // 随机数量,获得买1和卖1之间的价格
        BigDecimal randomNumber = RandomUtil.getRandom(data.getMinNumber(), data.getMaxNumber(), amountScale-3,amountScale);
        // 判断当前 1 分钟的涨跌幅,如果比较高,则交易量顺势增大
        if (randomNumber.compareTo(BigDecimal.ZERO) == 0) randomNumber = RandomUtil.getRandom(data.getMinNumber(), data.getMaxNumber(), amountScale,amountScale);
        MinChange minChange = minChangeCache.computeIfAbsent(pair, k -> new MinChange());
        BigDecimal change = minChange.getMinChange(marketPrice).abs();
        if (change.compareTo(BigDecimal.ZERO) == 0) change = new BigDecimal("0.01");
        randomNumber = randomNumber.multiply(change.multiply(new BigDecimal(100)));
        if (change.compareTo(new BigDecimal("0.03")) < 0){
            randomNumber = getRandomNumber(randomNumber); // 再随机点数字
        }


        OrderMessage sellOrder = MatchService.sellDepth.get(data.getPair()).getFirstItem();
        OrderMessage buyOrder = MatchService.buyDepth.get(data.getPair()).getFirstItem();
        if (sellOrder == null || buyOrder == null){
            return;
        }
        BigDecimal sellPrice = sellOrder.getPrice();
        BigDecimal buyPrice = buyOrder.getPrice();
        if (sellPrice == null || sellPrice.compareTo(BigDecimal.ZERO) == 0 || buyPrice == null || buyPrice.compareTo(BigDecimal.ZERO) == 0){
            return ;
        }
        if (sellPrice.subtract(buyPrice).compareTo(RandomUtil.getMinDecimal(priceScale)) <= 0){
            return ;
        }

        BigDecimal platformPrice = platformPriceCache.get(data.getPair());

        if (platformPrice == null ||  platformPrice.compareTo(sellPrice) >= 0 || platformPrice.compareTo(buyPrice) <= 0){
            // 其他平台价格在买一和卖一之外
            platformPrice = marketPrice;
        }

        BigDecimal[] rZt = getRZt(marketPrice);
        BigDecimal maxMarketPrice = rZt[1];
        BigDecimal minMarketPrice = rZt[0];

        int count = 0;
        while (maxMarketPrice.compareTo(buyPrice.add(RandomUtil.getMinDecimal(priceScale))) <= 0 ||
                minMarketPrice.compareTo(sellPrice.subtract(RandomUtil.getMinDecimal(priceScale))) >= 0 ||
                maxMarketPrice.subtract(minMarketPrice).compareTo(RandomUtil.getMinDecimal(priceScale))<0){
            count++;
            rZt = getRZt(marketPrice);
            maxMarketPrice = rZt[1];
            minMarketPrice = rZt[0];
            try {
                if (count == 10){
                    return;
                } else {
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        BigDecimal maxPlatformPrice = platformPrice.multiply(new BigDecimal("1.02"));
        BigDecimal minPlatformPrice = platformPrice.multiply(new BigDecimal("0.98"));
        if (maxMarketPrice.compareTo(maxPlatformPrice) > 0){
            maxMarketPrice = maxPlatformPrice;
        }
        if (maxMarketPrice.compareTo(sellPrice) > 0){
            maxMarketPrice = sellPrice;
        }
        if (minMarketPrice.compareTo(minPlatformPrice) < 0){
            minMarketPrice = minPlatformPrice;
        }
        if (minMarketPrice.compareTo(buyPrice) < 0){
            minMarketPrice = buyPrice;
        }

        BigDecimal price = RandomUtil.getRandom(minMarketPrice,maxMarketPrice,priceScale,priceScale);
        if (price == null) return;
        List<String> directions = RandomUtil.randomDirections();
        for (String direction : directions) {
            makeOrder(data.getPair(),price,randomNumber,direction);
        }
        minChange.put(price);
    }

    public static BigDecimal getPrice(){
        BigDecimal marketPrice = new BigDecimal("0.0185");
        int priceScale = 4;
        BigDecimal sellPrice = new BigDecimal("0.0188");
        BigDecimal buyPrice = new BigDecimal("0.0184");
        BigDecimal platformPrice = marketPrice;
        BigDecimal[] rZt = getRZt(marketPrice);
        BigDecimal maxMarketPrice = rZt[1];
        BigDecimal minMarketPrice = rZt[0];

        while (maxMarketPrice.compareTo(buyPrice.add(RandomUtil.getMinDecimal(priceScale))) <= 0 ||
                minMarketPrice.compareTo(sellPrice.subtract(RandomUtil.getMinDecimal(priceScale))) >= 0 ||
                maxMarketPrice.subtract(minMarketPrice).compareTo(RandomUtil.getMinDecimal(priceScale))<0){
            rZt = getRZt(marketPrice);
            maxMarketPrice = rZt[1];
            minMarketPrice = rZt[0];
        }

        BigDecimal maxPlatformPrice = platformPrice.multiply(new BigDecimal("1.02"));
        BigDecimal minPlatformPrice = platformPrice.multiply(new BigDecimal("0.98"));
        if (maxMarketPrice.compareTo(maxPlatformPrice) > 0){
            maxMarketPrice = maxPlatformPrice;
        }
        if (maxMarketPrice.compareTo(sellPrice) > 0){
            maxMarketPrice = sellPrice;
        }
        if (minMarketPrice.compareTo(minPlatformPrice) < 0){
            minMarketPrice = minPlatformPrice;
        }
        if (minMarketPrice.compareTo(buyPrice) < 0){
            minMarketPrice = buyPrice;
        }

        BigDecimal price = RandomUtil.getRandom(minMarketPrice,maxMarketPrice,priceScale,priceScale);
        return price;
    }


    public static BigDecimal[] getRZt(BigDecimal marketPrice){
        Double maxRate = RandomUtil.getZT(0.00002)+1;
        Double minRate = RandomUtil.getZT(0.00002)+1;
        if (maxRate < minRate){
            Double rate = maxRate;
            maxRate = minRate;
            minRate = rate;
        }
        BigDecimal maxMarketPrice = marketPrice.multiply(new BigDecimal(maxRate.toString()));
        BigDecimal minMarketPrice = marketPrice.multiply(new BigDecimal(minRate.toString()));
        return new BigDecimal[]{minMarketPrice,maxMarketPrice};
    }





    /**
     *  自动平衡币价
     */
    private BigDecimal doBalanceCoinPriceByPair(BalanceCoinPriceParams param, Map<String, BigDecimal> binancePriceMap, Map<String, BigDecimal> gateioPriceMap, Map<String, BigDecimal> huobiPriceMap) throws Exception{
        BaseContextHandler.setUserID(ScriptUser.BALANCE_PRICE_USER);
        String pair = param.getPair();
        BigDecimal marketPrice = klineService.getMarketPrice(pair);
        BigDecimal platformPrice = getPlatformPrice(param,binancePriceMap,gateioPriceMap,huobiPriceMap);
        if (platformPrice == null) return null;
        BigDecimal maxPrice = platformPrice.multiply(BigDecimal.ONE.add(param.getRate()));
        BigDecimal minPrice = platformPrice.multiply(BigDecimal.ONE.subtract(param.getRate()));
        OrderBook buyBook = MatchService.buyDepth.get(pair);
        OrderBook sellBook = MatchService.sellDepth.get(pair);

        if (platformPrice.compareTo(marketPrice) > 0){
            // 此时,拉盘,吃掉卖单,直到吃到 minPrice
            while (sellBook.book.size() > 0){
                OrderMessage sellItem = sellBook.getFirstItem();
                if (sellItem.getPrice().compareTo(minPrice) < 0){
                    this.makeOrder(pair,sellItem.getPrice(),sellItem.getRemain(), OrderDirection.BUY);
                    marketPrice = sellItem.getPrice();
                    Thread.sleep(50);
                } else {
                    return marketPrice;
                }
            }

        } else {
            // 此时砸盘,吃掉买单,直到吃到 maxPrice
            while (buyBook.book.size() > 0){
                OrderMessage buyItem = buyBook.getFirstItem();
                if (buyItem.getPrice().compareTo(maxPrice) > 0){
                    this.makeOrder(pair,buyItem.getPrice(),buyItem.getRemain(), OrderDirection.SELL);
                    marketPrice = buyItem.getPrice();
                    Thread.sleep(50);
                } else {
                    return marketPrice;
                }
            }
        }
        return marketPrice;

    }



    private void doSupplyOrderBook(BalanceCoinPriceParams param,BigDecimal marketPrice) {
        // 如果没有其他平台和 bitcola 的价格直接返回
        if (marketPrice == null) return ;
        BaseContextHandler.setUserID(ScriptUser.BALANCE_PRICE_USER);
        String pair = param.getPair();
        Integer amountScale = QueueService.scaleCache.get(pair).getAmountScale();
        OrderBook buyBook = MatchService.buyDepth.get(pair);
        OrderBook sellBook = MatchService.sellDepth.get(pair);

        // 1 取消掉安全阈值内的挂单
        BigDecimal maxSafePrice = marketPrice.multiply(BigDecimal.ONE.add(param.getSafeRate()));
        BigDecimal minSafePrice = marketPrice.multiply(BigDecimal.ONE.subtract(param.getSafeRate()));
        cancelOrderByCondition(buyBook,minSafePrice,true,param.getSafeNumber());
        cancelOrderByCondition(sellBook,maxSafePrice,false,param.getSafeNumber());


        BigDecimal minPrice = minSafePrice.multiply(BigDecimal.ONE.subtract(POINT_TWO));
        BigDecimal maxPrice = maxSafePrice.multiply(BigDecimal.ONE.add(POINT_TWO));
        // 3 不足30个订单则补齐安全阈值外的订单
        if (buyBook.book.size() < 30){
            int time = 30 - buyBook.book.size();
            //this.supplyOrder(pair,minPrice,minSafePrice,param.getSafeNumber(),OrderDirection.BUY,time);
        }
        if (sellBook.book.size() < 30){
            int time = 30 - sellBook.book.size();
            //this.supplyOrder(pair,maxSafePrice,maxPrice,param.getSafeNumber(),OrderDirection.SELL,time);
        }

        // 2 补齐安全阈值外 ~ 买1 或者卖1 的订单
        OrderMessage buyFirstItem = buyBook.getFirstItem();
        OrderMessage sellFirstItem = sellBook.getFirstItem();
        if (buyFirstItem != null && buyFirstItem.getPrice().compareTo(minSafePrice) < 0){
            minPrice = minPrice.max(buyFirstItem.getPrice());
            BigDecimal rate = minSafePrice.subtract(minPrice).divide(minSafePrice,5,RoundingMode.HALF_UP);
            if (rate.compareTo(param.getSupplyOrderRate()) > 0){ // 大于千分之5则补充
                long time = rate.divide(param.getSupplyOrderRate(), 10, RoundingMode.HALF_UP).longValue();
                //this.supplyOrder(pair,minPrice,minSafePrice,param.getSafeNumber(),OrderDirection.BUY,time);
            }
        }
        if (sellFirstItem != null && sellFirstItem.getPrice().compareTo(maxSafePrice) > 0) {
            maxPrice = maxSafePrice.min(sellFirstItem.getPrice());
            BigDecimal rate = maxPrice.subtract(maxSafePrice).divide(maxSafePrice,5,RoundingMode.HALF_UP);
            if (rate.compareTo(param.getSupplyOrderRate()) > 0){ // 大于千分之5则补充
                long time = rate.divide(param.getSupplyOrderRate(), 10, RoundingMode.HALF_UP).longValue();
                //this.supplyOrder(pair,maxPrice,maxSafePrice,param.getSafeNumber(),OrderDirection.SELL,time);
            }
        }


        // 4 在安全阈值内补齐小额订单
        //  判断阈值内的订单数量,  3 个数量,超过3个,则不补充
        int time = param.getSafeRate().divide(param.getSupplyOrderRate(), 0, RoundingMode.UP).intValue();
        int countSell = getOrderNumberByPrice(marketPrice,maxSafePrice,sellBook);
        if (countSell < time){
            //supplyOrder(pair,marketPrice,maxSafePrice,param.getSafeNumber().divide(TWO),OrderDirection.SELL,time-countSell);
        }
        int countBuy = getOrderNumberByPrice(minSafePrice,marketPrice,buyBook);
        if (countBuy < time){
            //supplyOrder(pair,minSafePrice,marketPrice,param.getSafeNumber().divide(TWO),OrderDirection.BUY,time-countBuy);
        }
    }

    private int getOrderNumberByPrice(BigDecimal minPrice, BigDecimal maxPrice, OrderBook orderBook) {
        Iterator<OrderMessage> iterator = orderBook.book.keySet().iterator();
        int count = 0;
        while (iterator.hasNext()){
            BigDecimal price = iterator.next().getPrice();
            if (price.compareTo(minPrice) > 0 && price.compareTo(maxPrice) < 0){
                count++;
            }
        }
        return count;
    }

    /**
     * 补齐订单,按照最价格,最大价格,数量在大小之间随机,
     * @param time 次数
     */
    private void supplyOrder(String pair,BigDecimal minPrice, BigDecimal maxPrice,BigDecimal avgNumber, String direction, long time) {
        Integer amountScale = QueueService.scaleCache.get(pair).getAmountScale();
        Integer priceScale = QueueService.scaleCache.get(pair).getPriceScale();
        BigDecimal cha = maxPrice.subtract(minPrice).divide(new BigDecimal(time), priceScale, RoundingMode.HALF_UP);
        BigDecimal price = minPrice.add(RandomUtil.getMinDecimal(priceScale));
        for (int i = 0; i < time; i++) {
            BigDecimal number;
            if (direction.equals(OrderDirection.SELL)){
                number = RandomUtil.getRandom(avgNumber,avgNumber.multiply(new BigDecimal(i+1)),amountScale-2,amountScale);
            } else {
                number = RandomUtil.getRandom(avgNumber,avgNumber.multiply(new BigDecimal(time-i)),amountScale-2,amountScale);
            }
            if (number == null) number = avgNumber;
            if (i  == 0) number = RandomUtil.getZtDecimal(number,10);
            makeOrder(pair,price,number,direction);
            price = RandomUtil.getRandom(price,price.add(cha.multiply(TWO)),priceScale-2,priceScale);
        }
    }


    private void makeOrderAndCancel(String pair,BigDecimal price,BigDecimal number,String direction){
        BaseContextHandler.setUserID(ScriptUser.NO_MATCH_USER);
        try {
            OrderMessage order = makeOrder(pair, price, number, direction);
            if (order != null){
                Thread.sleep(new Random().nextInt(3000)+2000);
                order.setType(OrderType.CANCEL);
                matchOrderQueue.putMessage(order.getPair(),order);
                ColaExchangeController.count.incrementAndGet();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    public OrderMessage makeOrder(String pair,BigDecimal price,BigDecimal number,String direction) {
        Integer amountScale = QueueService.scaleCache.get(pair).getAmountScale();
        Integer priceScale = QueueService.scaleCache.get(pair).getPriceScale();
        price = price.setScale(priceScale,RoundingMode.DOWN);
        number = number.setScale(amountScale,RoundingMode.DOWN);
        if (price.compareTo(BigDecimal.ZERO)<=0) return null;
        if (number.compareTo(BigDecimal.ZERO)<=0) return null;

        OrderMessage order = new OrderMessage();
        String orderId = snowflake.nextIdStr();
        order.setId(orderId);
        order.setPrice(price);
        order.setNumber(number);
        order.setRemain(order.getNumber());
        order.setTimestamp(System.currentTimeMillis());
        order.setUserId(BaseContextHandler.getUserID());
        order.setType(OrderType.LIMIT);
        order.setDirection(direction);
        order.setPair(pair);
        // 创建手续费为0的交易
        if (!order.getUserId().equals(ScriptUser.NO_MATCH_USER)){
            int i = biz.makeOrder(order, BigDecimal.ZERO);
            if (i == -1) {
                log.error("no money error");
                throw new MakeOrderNoMoneyException();
            }
            if (i == 0) {
                log.error("交易异常");
                throw new MakeOrderException(100,"error");
            }
        } else {
            order.setStatus(OrderStatus.PENDING);
            order.setFeeRate(BigDecimal.ZERO);
            order.setAveragePrice(order.getPrice());
        }
        matchOrderQueue.putMessage(order.getPair(),order);
        ColaExchangeController.count.incrementAndGet();
        return order;
    }


    private BigDecimal getPlatformPrice(BalanceCoinPriceParams param, Map<String, BigDecimal> binancePriceMap, Map<String, BigDecimal> gateioPriceMap, Map<String, BigDecimal> huobiPriceMap){
        String pair = param.getPair();
        Integer priceScale = scaleCache.get(pair).getPriceScale();
        if (param.isBitcola()){
            BigDecimal coinCodePrice = klineService.getMarketPrice(getUsdtPair(param.coinCode()));
            BigDecimal symbolPrice = klineService.getMarketPrice(getUsdtPair(param.symbol()));
            return coinCodePrice.divide(symbolPrice,priceScale,RoundingMode.HALF_UP);
        }
        BigDecimal binancePrice = binancePriceMap.get(pair);
        BigDecimal gateioPrice = gateioPriceMap.get(pair);
        BigDecimal huobiPrice = huobiPriceMap.get(pair);
        // 获取系统价格和平台价格比较,计算权重
        double binanceWeight = param.getBinanceWeight();
        double gateioWeight = param.getGateioWeight();
        double huobiWeight = param.getHuobiWeight();
        if (binancePrice == null) {
            binancePrice = getPairPrice(binancePriceMap,param.coinCode(),param.symbol(),priceScale);
            if (binancePrice.compareTo(BigDecimal.ZERO) == 0){
                binanceWeight = 0;
            }
        }
        if (gateioPrice == null) {
            gateioPrice = getPairPrice(gateioPriceMap,param.coinCode(),param.symbol(),priceScale);
            if (gateioPrice.compareTo(BigDecimal.ZERO) == 0){
                gateioWeight = 0;
            }
        }
        if (huobiPrice == null) {
            huobiPrice = getPairPrice(huobiPriceMap,param.coinCode(),param.symbol(),priceScale);
            if (huobiPrice.compareTo(BigDecimal.ZERO) == 0){
                huobiWeight = 0;
            }
        }
        if ((binanceWeight ==0 && binancePrice.compareTo(BigDecimal.ZERO) == 0 ) ||
                (gateioWeight ==0 && gateioPrice.compareTo(BigDecimal.ZERO) == 0 ) ||
                (huobiWeight ==0 && huobiPrice.compareTo(BigDecimal.ZERO) == 0)) {
            System.out.println("获取价格出错,本次平衡价格取消");
            throw new RuntimeException("获取价格出错,本次平衡价格取消");
        }
        double total = binanceWeight + gateioWeight + huobiWeight;
        if (total == 0){
            return null;
        }
        //BigDecimal platformPrice = binancePrice.multiply(new BigDecimal(String.valueOf(binanceWeight/total)))
        //        .add(gateioPrice.multiply(new BigDecimal(String.valueOf(gateioWeight/total))))
        //        .add(huobiPrice.multiply(new BigDecimal(String.valueOf(huobiWeight/total))));
        BigDecimal platformPrice;

        //  寻找与平台价格差值最小的
        BigDecimal marketPrice  = klineService.getMarketPrice(pair);
        BigDecimal binanceSUB = BigDecimal.ZERO;
        BigDecimal huobiSUB = BigDecimal.ZERO;
        BigDecimal gateioSUB = BigDecimal.ZERO;
        BigDecimal min = new BigDecimal("-1");
        if (binanceWeight !=0){
            binanceSUB = binancePrice.subtract(marketPrice).abs();
            if (min.compareTo(BigDecimal.ZERO) > 0){
                min = min.min(binanceSUB);
            } else {
                min = binanceSUB;
            }
        }
        if (huobiWeight !=0){
            huobiSUB = huobiPrice.subtract(marketPrice).abs();
            if (min.compareTo(BigDecimal.ZERO) > 0){
                min = min.min(huobiSUB);
            } else {
                min = huobiSUB;
            }
        }
        if (gateioWeight != 0){
            gateioSUB = gateioPrice.subtract(marketPrice).abs();
            if (min.compareTo(BigDecimal.ZERO) > 0){
                min = min.min(gateioSUB);
            } else {
                min = gateioSUB;
            }
        }
        if (min.compareTo(binanceSUB) == 0){
            platformPrice = binancePrice;
        } else if (min.compareTo(huobiSUB) == 0){
            platformPrice = huobiPrice;
        } else if (min.compareTo(gateioSUB) == 0){
            platformPrice = gateioPrice;
        } else {
            throw new RuntimeException("未找到合适价格");
        }
        // 保存每个交易对的价格,可供自动交易脚本使用
        platformPriceCache.put(pair,platformPrice);
        return platformPrice;
    }


    private static BigDecimal getRandomNumber(BigDecimal randomNumber){
        char c = UUID.randomUUID().toString().charAt(0);
        if (c == '1') {
            randomNumber = randomNumber.multiply(BigDecimal.TEN);
        } else if (c == '2') {
            randomNumber = randomNumber.multiply(FIVE);
        }
        return randomNumber;
    }

    private String getUsdtPair(String coinCode){
        return coinCode+"_"+USDT;
    }

    private BigDecimal getPairPrice(Map<String,BigDecimal> priceMap,String coinCode,String symbol,int priceScale){
        BigDecimal coinCodePrice = priceMap.get(getUsdtPair(coinCode));
        BigDecimal symbolPrice = priceMap.get(getUsdtPair(symbol));
        if (coinCodePrice == null || symbolPrice == null){
            return BigDecimal.ZERO;
        }
        return coinCodePrice.divide(symbolPrice,priceScale,RoundingMode.HALF_UP);
    }
    private void cancelOrderByCondition(OrderBook orderBook,BigDecimal price,boolean isBuyBook,BigDecimal safeNumber){
        List<OrderMessage> cancelOrders = new ArrayList<>();
        for (OrderMessage key : orderBook.book.keySet()) {
            if (isBuyBook && key.getPrice().compareTo(price) > 0){
                if (orderBook.book.get(key).getRemain().compareTo(safeNumber)>0){
                    if (key.getUserId().equals(ScriptUser.BALANCE_PRICE_USER)){
                        cancelOrders.add(key);
                    }
                }
            } else if (!isBuyBook && key.getPrice().compareTo(price) < 0){
                if (orderBook.book.get(key).getRemain().compareTo(safeNumber)>0) {
                    if (key.getUserId().equals(ScriptUser.BALANCE_PRICE_USER)){
                        cancelOrders.add(key);
                    }
                }
            }
        }
        for (OrderMessage order : cancelOrders) {
            order.setType(OrderType.CANCEL);
            matchOrderQueue.putMessage(order.getPair(),order);
            ColaExchangeController.count.incrementAndGet();
        }
    }


    @Scheduled(cron = "0 0/20 * * * ?")
    public void scheduled(){
        BaseContextHandler.setUserID(ScriptUser.BALANCE_PRICE_USER);
        List<String> pairs = new ArrayList<>();
        pairs.add("BTC_USDT");
        pairs.add("EOS_USDT");
        pairs.add("LTC_USDT");
        pairs.add("ETH_USDT");
        Map<String,Map<String,BigDecimal>> pairNumber = new HashMap<>();
        Map<String,BigDecimal> btc = new HashMap<>();
        btc.put("min",new BigDecimal("0.0005"));
        btc.put("max",new BigDecimal("0.8"));
        pairNumber.put("BTC_USDT",btc);
        Map<String,BigDecimal> eos = new HashMap<>();
        eos.put("min",new BigDecimal("0.05"));
        eos.put("max",new BigDecimal("200"));
        pairNumber.put("EOS_USDT",eos);
        Map<String,BigDecimal> eth = new HashMap<>();
        eth.put("min",new BigDecimal("0.005"));
        eth.put("max",new BigDecimal("10"));
        pairNumber.put("ETH_USDT",eth);
        Map<String,BigDecimal> ltc = new HashMap<>();
        ltc.put("min",new BigDecimal("0.001"));
        ltc.put("max",new BigDecimal("20"));
        pairNumber.put("LTC_USDT",ltc);

        for (String pair : pairs) {
            try {
                dealScheduled(pair,pairNumber.get(pair));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void dealScheduled(String pair, Map<String, BigDecimal> map) {
        BigDecimal minNumber = map.get("min");
        BigDecimal maxNumber = map.get("max");

        // 获得买一,卖一,当前价格
        BigDecimal marketPrice = klineService.getMarketPrice(pair);
        OrderMessage sellOrder = MatchService.sellDepth.get(pair).getFirstItem();
        OrderMessage buyOrder = MatchService.buyDepth.get(pair).getFirstItem();
        BigDecimal sellFirstPrice = sellOrder.getPrice();
        BigDecimal buyFirstPrice = buyOrder.getPrice();

        BigDecimal sellRate = sellFirstPrice.subtract(marketPrice).divide(marketPrice, 6, RoundingMode.HALF_UP);
        BigDecimal buyRate = marketPrice.subtract(buyFirstPrice).divide(marketPrice, 6, RoundingMode.HALF_UP);
        BigDecimal sellPrice = marketPrice;
        BigDecimal buyPrice = marketPrice;
        if (sellRate.compareTo(new BigDecimal("0.02")) >=0){
            for (int i = 0; i < 10; i++) {
                sellPrice = sellPrice.multiply(BigDecimal.ONE.add((new BigDecimal("0.002"))));
                BigDecimal number = RandomUtil.getRandom(minNumber,maxNumber,8,8);
                this.makeOrder(pair,sellPrice,number,OrderDirection.SELL);
            }
        }
        if (buyRate.compareTo(new BigDecimal("0.02")) >=0){
            for (int i = 0; i < 10; i++) {
                buyPrice = buyPrice.multiply(BigDecimal.ONE.subtract((new BigDecimal("0.002"))));
                BigDecimal number = RandomUtil.getRandom(minNumber,maxNumber,8,8);
                this.makeOrder(pair,buyPrice,number,OrderDirection.BUY);
            }
        }

    }


}
