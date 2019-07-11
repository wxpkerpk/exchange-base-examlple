package com.bitcola.exchange.script.queue;

import java.util.Random;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author zkq
 * @create 2019-03-18 15:51
 **/
public class ScriptDelayQueue<T> {

    private DelayQueue<DelayMessage<T>> queue = new DelayQueue<>();

    /**
     *
     * @param msg 消息
     * @param num 每分钟执行次数
     */
    public void putMessage(T msg,int num,TimeUnit unit){
        long time = num *  TimeUnit.DAYS.toMillis(1) / unit.toMillis(1);
        int remain = (int) time;
        int DAY_1 = 1000 * 60 * 60 * 24;
        int millis = 0;
        for (int i = 0; i < time; i++) {
            int avg = (DAY_1 - millis) / remain;
            millis += new Random().nextInt(avg * 2 + 1);
            queue.put(new DelayMessage<>(millis, TimeUnit.MILLISECONDS,msg));
            remain--;
        }
    }

    public DelayMessage<T> getMessage(){
        try {
            return queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void clear(){
        queue.clear();
    }
}
