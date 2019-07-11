package com.bitcola.exchange.entity;


import com.bitcola.exchange.constant.OrderDirection;
import com.bitcola.exchange.message.OrderMessage;
import net.sf.jsqlparser.util.deparser.OrderByDeParser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author zkq
 * @create 2019-02-11 14:42
 **/
public class OrderBook{

    public final ConcurrentSkipListMap<OrderMessage, OrderMessage> book;
    public final String direction;

    static final Comparator<OrderMessage> SORT_BUY = (o1, o2) -> {
        int cmp = o2.price.compareTo(o1.price);
        cmp = cmp == 0 ? Long.compare(o1.timestamp,o2.timestamp) : cmp;
        cmp = cmp == 0 ? o2.number.compareTo(o1.number) : cmp;
        cmp = cmp == 0 ? Long.compare(Long.valueOf(o1.id),Long.valueOf(o2.id)) : cmp;
        return cmp;
    };
    static final Comparator<OrderMessage> SORT_SELL = (o1, o2) -> {
        int cmp = o1.price.compareTo(o2.price);
        cmp = cmp == 0 ? Long.compare(o2.timestamp, o1.timestamp) : cmp;
        cmp = cmp == 0 ? o1.number.compareTo(o2.number) : cmp;
        cmp = cmp == 0 ? Long.compare(Long.valueOf(o2.id), Long.valueOf(o1.id)) : cmp;
        return cmp;
    };

    public OrderBook(boolean isBuy){
        if (isBuy){
            book = new ConcurrentSkipListMap<OrderMessage, OrderMessage>(SORT_BUY);
            direction = OrderDirection.BUY;
        } else {
            book = new ConcurrentSkipListMap<OrderMessage, OrderMessage>(SORT_SELL);
            direction = OrderDirection.SELL;
        }
    }

    public OrderMessage getFirstKey(){
        return this.book.isEmpty() ? null :this.book.firstKey();
    }

    public OrderMessage getFirstItem(){
        OrderMessage key = getFirstKey();
        if (key == null) return null;
        return this.book.get(key);
    }

    public OrderMessage remove(OrderMessage key){
        return book.remove(key);
    }

    public OrderMessage put(OrderMessage message){
        this.book.put(message,message);
        return message;
    }

    public List<BigDecimal[]> getDepth(int size, int scale){
        List<BigDecimal[]> depths = new ArrayList<>(size);
        BigDecimal[] lastDepth = null;
        int i = 1;
        for (Map.Entry<OrderMessage, OrderMessage> entry : book.entrySet()) {
            OrderMessage order = entry.getValue();
            if (lastDepth == null){
                lastDepth = new BigDecimal[]{order.getPrice().setScale(scale, RoundingMode.DOWN),order.getRemain()};
                depths.add(lastDepth);
                i++;
            } else {
                if (order.getPrice().setScale(scale,RoundingMode.DOWN).compareTo(lastDepth[0])==0){
                    lastDepth[1] = lastDepth[1].add(order.getRemain());
                } else {
                    if (i > size){
                        break;
                    }
                    lastDepth = new BigDecimal[]{order.getPrice().setScale(scale, RoundingMode.DOWN),order.getRemain()};
                    depths.add(lastDepth);
                    i++;
                }
            }
        }
        // 填充不足的
        //for (; i <= size; i++) {
        //    lastDepth = new BigDecimal[]{BigDecimal.ZERO,BigDecimal.ZERO};
        //    depths.add(lastDepth);
        //}
        return depths;
    }

    public BigDecimal getSamePriceNumber(BigDecimal price){
        BigDecimal number = BigDecimal.ZERO;
        for (Map.Entry<OrderMessage, OrderMessage> entry : book.entrySet()) {
            OrderMessage order = entry.getValue();
            if (order.getPrice().compareTo(price)==0){
                number = number.add(order.getRemain());
            }
        }
        return number;
    }


}
