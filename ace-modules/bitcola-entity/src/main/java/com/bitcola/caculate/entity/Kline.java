package com.bitcola.caculate.entity;

import lombok.Data;

import java.util.List;

/*
 * @author:wx
 * @description:
 * @create:2018-08-11  00:15
 */
@Data
public class Kline {
    double open;
    double high;
    double low;
    double close;
    double vol;
    long time;
    public Number[]toArray()
    {
        return new Number[]{time,open,high,low,close,vol};


    }

}
