package com.bitcola.dataservice.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Snowflake {


    private final long twepoch = 1288834974657L;
    private final long workerIdBits = 5L;
    private final long datacenterIdBits = 5L;
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
    private final long sequenceBits = 12L;
    private final long workerIdShift = sequenceBits;
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    private long workerId;
    private long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public Snowflake() {
        this.workerId = Math.abs(new Random().nextInt()%32);
        this.datacenterId = Math.abs(new Random().nextInt()%32);
    }

    public Snowflake(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public String nextIdStr(){
        return String.valueOf(nextId());

    }

    public synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampLeftShift) | (datacenterId << datacenterIdShift) | (workerId << workerIdShift) | sequence;
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        Snowflake idWorke1 = new Snowflake();
        Snowflake idWorker2 = new Snowflake();
        Snowflake idWorker3 = new Snowflake();
        Snowflake idWorker4 = new Snowflake();


        long start=System.currentTimeMillis();
        long sum=0;
        Map<String ,Integer>booleanMap=new HashMap<>();
        for (int i = 0; i < 1000000; i++) {
            booleanMap.putIfAbsent(idWorke1.nextIdStr(),1);
            booleanMap.putIfAbsent(idWorker2.nextIdStr(),1);
            booleanMap.putIfAbsent(idWorker3.nextIdStr(),1);
            booleanMap.putIfAbsent(idWorker4.nextIdStr(),1);
        }
        long end=System.currentTimeMillis();
        System.out.println(end-start);
        System.out.println(booleanMap.size());
    }
}
