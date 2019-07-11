package com.bitcola.exchange.caculate.data;

import org.springframework.data.redis.connection.Message;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/*
 * @author:wx
 * @description:
 * @create:2018-11-15  00:04
 */
public class TransactionMessage  implements Delayed {
    private int id;
    private String body; // 消息内容
    private long excuteTime;// 延迟时长，这个是必须的属性因为要按照这个判断延时时长。
    String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public long getExcuteTime() {
        return excuteTime;
    }

    public TransactionMessage(int id, String body, long delayTime) {
        this.id = id;
        this.body = body;
        this.excuteTime = TimeUnit.NANOSECONDS.convert(delayTime, TimeUnit.MILLISECONDS) + System.nanoTime();
    }

    // 自定义实现比较方法返回 1 0 -1三个参数
    @Override
    public int compareTo(Delayed delayed) {
        TransactionMessage msg = (TransactionMessage) delayed;
        return Integer.compare(this.id, msg.id);
    }

    // 延迟任务是否到时就是按照这个方法判断如果返回的是负数则说明到期否则还没到期
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.excuteTime - System.nanoTime(), TimeUnit.NANOSECONDS);
    }
}

