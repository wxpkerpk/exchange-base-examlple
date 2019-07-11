package com.bitcola.exchange.script.queue;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author zkq
 * @create 2019-03-18 15:50
 **/
public class DelayMessage<T> implements Delayed {

    long executeTime;
    T data;

    DelayMessage(long delay, TimeUnit unit, T data){
        this.executeTime = unit.toNanos(delay) + System.nanoTime();
        this.data = data;
    }

    DelayMessage(long delay,T data){
        this.executeTime = TimeUnit.MILLISECONDS.toNanos(delay) + System.nanoTime();
        this.data = data;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.executeTime - System.nanoTime(),TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(this.getDelay(TimeUnit.NANOSECONDS),o.getDelay(TimeUnit.NANOSECONDS));
    }

    public T getData(){
        return data;
    }
}