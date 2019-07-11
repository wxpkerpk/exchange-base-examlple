package com.bitcola.exchange.queue;

/**
 * @author zkq
 * @create 2019-02-12 12:43
 **/
public abstract class AbstractMessageQueue<T> {

    /**
     * 获得队列中的下一个
     * @param pair 交易对
     * @return
     */
    public abstract Object get(String pair);

    /**
     * 放入队列,返回队列长度
     * @param t
     * @return
     */
    public abstract Long put(T t);

    /**
     * 清空 redis 队列
     * @param pair
     */
    public abstract Boolean clear(String pair);
}
