package com.bitcola.exchange.service;

import com.bitcola.exchange.constant.MatchMessageType;
import com.bitcola.exchange.constant.OrderDirection;
import com.bitcola.exchange.constant.OrderStatus;
import com.bitcola.exchange.constant.OrderType;
import com.bitcola.exchange.entity.OrderBook;
import com.bitcola.exchange.message.OrderMessage;
import com.bitcola.exchange.mapper.ColaExchangeMapper;
import com.bitcola.exchange.mapper.OrderMapper;
import com.bitcola.exchange.message.*;
import com.bitcola.exchange.queue.*;
import com.bitcola.exchange.script.ScriptUser;
import com.bitcola.exchange.util.InFluxDbService;
import com.bitcola.exchange.websocket.DepthNotifyMessage;
import com.bitcola.exchange.websocket.KlineNotifyMessage;
import com.bitcola.exchange.websocket.PriceNotifyMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;


/**
 * 1    3个消息队列 ( 订单队列,成交记录队列,推送队列)
 *          队列采用 redis 持久化
 *          其他队列,提供 get() 方法,获取队列中的一条记录,并移除队列中的数据
 *          深度使用内存红黑树
 *
 * 2    初始化
 *          加载交易对,开启撮合线程,开启清算线程,开启推送线程
 *
 * 3    下单
 *          新增下单记录,冻结用户资金,下单记录保存到订单队列
 *
 * 4    撮合
 *          取出订单队列,与深度队列撮合,生成成交记录队列和推送队列
 *
 * 5    清算
 *          批量插入日志,资金结算
 *
 * 6    展示
 *          k 线,深度,成交记录
 *
 * 7    业务
 *          取消订单,价格预估,开放交易对等...
 *
 * 8    取消订单
 *          移除深度上面的内存数据,放入结算队列
 *          解冻资金( 下单冻结数量 - 已成交数量 )
 *          改变订单状态
 *
 * 9    转移
 *          加入项目, redis 采用单独的 redis
 *          表字段,按照业务增加
 *          接入用户数据,资金签名
 *
 *
 * @author zkq
 * @create 2019-02-12 12:30
 **/
@Log4j2
@Service
public class MatchService implements ApplicationRunner, Ordered {

    @Resource(name = "matchOrderQueue")
    LinkedBlockingQueueMap<OrderMessage> matchOrderQueue;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    ColaExchangeMapper exchangeMapper;

    @Autowired
    InFluxDbService inFluxDbService;

    @Autowired
    KlineService klineService;

    @Resource(name = "klineInsertQueue")
    LinkedBlockingQueue<KLineInsertMessage> klineInsertQueue;

    @Resource(name = "clearOrderQueue")
    BitColaBlockingQueueMap<MatchMessage> clearOrderQueue;

    @Resource(name = "priceNotifyQueue")
    DelayQueueBySpeedMap<PriceNotifyMessage > priceNotifyQueue;

    @Resource(name = "depthNotifyQueue")
    DelayQueueBySpeedMap<DepthNotifyMessage> depthNotifyQueue;

    @Resource(name = "kLineNotifyQueue")
    DelayQueueBySpeedMap<KlineNotifyMessage> kLineNotifyQueue;

    public static final Map<String, OrderBook> buyDepth = new ConcurrentHashMap<>();
    public static final Map<String, OrderBook> sellDepth = new ConcurrentHashMap<>();
    public static final Map<String, Boolean> running = new ConcurrentHashMap<>();

    public static BigDecimal TWO = new BigDecimal("2");

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> strings = exchangeMapper.getAllPair();
        for (String pair : strings) {
            startPairThread(pair);
        }
        log.info("撮合服务 已经启动");
    }


    /**
     * 开启一个交易对撮合线程
     * @param pair 交易对
     */
    public void startPairThread(String pair){
        running.put(pair,false);
        OrderBook buyOrderBook = new OrderBook(true);
        OrderBook sellOrderBook = new OrderBook(false);
        MatchService.buyDepth.put(pair,buyOrderBook);
        MatchService.sellDepth.put(pair,sellOrderBook);
        long total = initDepth(pair);
        log.info(pair+" 初始化成功,共加载: "+total+" 笔订单");
        ExecutorService executors = Executors.newFixedThreadPool(1);
        executors.submit(()->{
            match(pair);
        });
    }

    private long initDepth(String pair){
        long total = 0;
        int page = 1;
        while(true){
            List<OrderMessage> orderEntities = orderMapper.selectUnSuccessOrder(pair,500, page);
            for (OrderMessage entity : orderEntities) {
                matchOrderQueue.putMessage(pair,entity);
            }
            page ++ ;
            total+=orderEntities.size();
            if (orderEntities.size() < 500) return total;
        }
    }

    /**
     * 开始撮合
     * @param pair
     */
    private void match(String pair){
        running.put(pair,true);
        while(true){
            try {
                OrderMessage order = matchOrderQueue.getMessage(pair);
                if (order != null) {
                    processOrder(order);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理不同类型的订单
     * @param order
     */
    private void processOrder(OrderMessage order) {
        OrderBook makerBook = order.getDirection().equals(OrderDirection.BUY) ? sellDepth.get(order.getPair()) : buyDepth.get(order.getPair());
        OrderBook takerBook = order.getDirection().equals(OrderDirection.BUY) ? buyDepth.get(order.getPair()) : sellDepth.get(order.getPair());
        if (OrderType.LIMIT.equals(order.getType())){
            doLimitOrder(order, makerBook, takerBook);
        } else if (OrderType.CANCEL.equals(order.getType())){
            doCancelOrder(order,takerBook);
        } else {
            throw new RuntimeException("还未实现此种类型的订单: "+order.getType());
        }
    }


    @Autowired
    RushService rushService;

    /**
     * 处理限价单
     * @param taker
     * @param makerBook 挂单队列
     * @param takerBook 未撮合进入此队列
     */
    private void doLimitOrder(OrderMessage taker, OrderBook makerBook, OrderBook takerBook) {
        final String takerId = taker.getId();
        final String pair = taker.getPair();
        final long currentTimeMillis = taker.getTimestamp();
        MatchMessage matchMessage = new MatchMessage();
        matchMessage.setPair(taker.getPair());
        matchMessage.setOrderId(taker.getId());
        matchMessage.setTimestamp(currentTimeMillis);
        matchMessage.getOrderMap().put(takerId,taker);
        DepthNotifyMessage depthMessage = new DepthNotifyMessage();
        boolean priceChange =false;
        for (;;) {
            if (pair.equals(rushService.getRushPair()) && !taker.getUserId().equals(rushService.getRushProjectUserId())){
                if (rushService.isOutOfMaxLimit(taker.getPrice().multiply(taker.getNumber()),taker.getUserId())){
                    break;
                }
            }
            OrderMessage maker = makerBook.getFirstItem();
            if (maker == null) {
                // empty order book:
                break;
            }
            if (taker.direction.equals(OrderDirection.BUY) && taker.price.compareTo(maker.price) < 0) {
                break;
            } else if (taker.direction.equals(OrderDirection.SELL) && taker.price.compareTo(maker.price) > 0) {
                break;
            }
            // 脚本不撮合判断
            if (taker.getUserId().equals(ScriptUser.NO_MATCH_USER)){
                return;
            }
            if (maker.getUserId().equals(ScriptUser.NO_MATCH_USER)){
                makerBook.remove(maker);
                break;
            }

            if (maker.price.compareTo(klineService.getMarketPrice(pair))!=0){
                priceChange = true;
            }
            // max amount to exchange:
            BigDecimal amount = taker.remain.min(maker.remain);
            taker.remain = taker.remain.subtract(amount);
            maker.remain = maker.remain.subtract(amount);
            // is maker fully filled?
            String makerStatus = maker.remain.compareTo(BigDecimal.ZERO) == 0 ? OrderStatus.FULL_COMPLETED
                    : OrderStatus.PARTIAL_COMPLETED;
            maker.setStatus(makerStatus);

            BigDecimal remain = makerBook.getSamePriceNumber(maker.getPrice());
            if (taker.getDirection().equals(OrderDirection.BUY)){
                depthMessage.getAsk().put(maker.price,remain);
            } else {
                depthMessage.getBids().put(maker.price,remain);
            }

            //  记录购买数量
            if (pair.equals(rushService.getRushPair()) && !taker.getUserId().equals(rushService.getRushProjectUserId())){
                rushService.putRushLimit(taker.getPrice().multiply(amount),taker.getUserId());
            }

            try {
                klineInsertQueue.put(new KLineInsertMessage(pair,maker.price,amount,currentTimeMillis));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MatchRecordMessage matchRecord = new MatchRecordMessage(taker.getId(),maker.getId(),maker.price,amount,makerStatus);
            matchMessage.addMatchRecord(matchRecord);
            matchMessage.getOrderMap().put(maker.getId(),maker);
            // should remove maker from order book?
            if (makerStatus.equals(OrderStatus.FULL_COMPLETED)) {
                makerBook.remove(maker);
            }
            // should remove taker from order book?
            if (taker.remain.compareTo(BigDecimal.ZERO) == 0) {
                taker = null;
                break;
            }
        }

        // 吃单剩余放入深度
        if (taker != null) {
            takerBook.put(taker);
            if (taker.getDirection().equals(OrderDirection.SELL)){
                depthMessage.getAsk().put(taker.price,takerBook.getSamePriceNumber(taker.getPrice()));
            } else {
                depthMessage.getBids().put(taker.price,takerBook.getSamePriceNumber(taker.getPrice()));
            }
            if (taker.getUserId().equals(ScriptUser.NO_MATCH_USER)){
                return;
            }
        }

        if (matchMessage.getMatchRecords().size() > 0){
            String takerStatus = taker == null ? OrderStatus.FULL_COMPLETED : OrderStatus.PARTIAL_COMPLETED;
            matchMessage.setTakerStatus(takerStatus);
            matchMessage.getOrderMap().get(takerId).setStatus(takerStatus);
            matchMessage.setType(MatchMessageType.MATCH_RESULT);
            matchMessage.setTakerRemain(matchMessage.getOrderMap().get(takerId).getRemain());
            clearOrderQueue.putMessage(pair,matchMessage);
            priceNotifyQueue.putMessage(pair,new PriceNotifyMessage());
        }
        // 推送深度,k线
        depthNotifyQueue.putMessage(pair,depthMessage);
        if (priceChange){
            kLineNotifyQueue.putMessage(pair,new KlineNotifyMessage());
        }
    }

    /**
     * 处理关闭订单
     * @param order
     * @param takerBook
     */
    private void doCancelOrder(OrderMessage order,OrderBook takerBook) {
        OrderMessage remove = takerBook.remove(order);
        if (remove == null){
            if (!order.getUserId().equals(ScriptUser.NO_MATCH_USER)){
                log.error("no such order item exist; 订单 id: "+order.getId());
            }
            return;
        }
        DepthNotifyMessage depthMessage = new DepthNotifyMessage();
        if (order.getDirection().equals(OrderDirection.SELL)){
            depthMessage.getAsk().put(order.price,takerBook.getSamePriceNumber(order.getPrice()));
        } else {
            depthMessage.getBids().put(order.price,takerBook.getSamePriceNumber(order.getPrice()));
        }
        depthNotifyQueue.putMessage(order.getPair(),depthMessage);
        if (remove.getUserId().equals(ScriptUser.NO_MATCH_USER)){
            return;
        }
        MatchMessage matchMessage = new MatchMessage();
        matchMessage.setPair(order.getPair());
        matchMessage.setOrderId(order.getId());
        matchMessage.setType(MatchMessageType.MATCH_CANCEL);
        String orderStatus = OrderStatus.FULL_CANCELLED;
        if (remove.getStatus().equals(OrderStatus.PARTIAL_COMPLETED)){
            orderStatus = OrderStatus.PARTIAL_CANCELLED;
        }
        remove.setStatus(orderStatus);
        matchMessage.setTakerStatus(orderStatus);
        matchMessage.getOrderMap().put(order.getId(),remove);
        clearOrderQueue.putMessage(order.getPair(),matchMessage);

    }

    @Override
    public int getOrder() {
        return 10;
    }
}
