package com.bitcola.exchange.launchpad.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DelayQueueBySpeed<T> {

    BlockingQueue<T> queue = new LinkedBlockingQueue<>();
    BlockingQueue<List<T>> message = new LinkedBlockingQueue<>();
    int maxDelay;
    int minDelay;
    float speedPerCount;

    void produceMessage() {
        new Thread(() -> {
            int sleepTime = minDelay;
            while (true) {
                List<T> list = new ArrayList<>();
                while (!queue.isEmpty()) {
                    int size = queue.size();
                    sleepTime = (int) (size * speedPerCount);
                    if (sleepTime > maxDelay) sleepTime = maxDelay;
                    if (sleepTime < minDelay) sleepTime = minDelay;
                    list.add(queue.poll());
                }
                try {
                    if (list.size()>0){
                        message.put(list);
                    }
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public DelayQueueBySpeed(int maxDelay, int minDelay, float speedPerCount) {
        this.maxDelay = maxDelay;
        this.minDelay = minDelay;
        this.speedPerCount = speedPerCount;
        produceMessage();
    }


    public void putMessage(T m) {
        try {
            queue.put(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<T> getMessage() {
        try {
            return message.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();

    }

}

