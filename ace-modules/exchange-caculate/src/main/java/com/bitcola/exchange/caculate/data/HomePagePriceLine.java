package com.bitcola.exchange.caculate.data;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/*
 * @author:wx
 * @description:首页数据
 * @create:2018-08-11  21:21
 */


@Data
public class HomePagePriceLine implements Serializable {
    double price;
    float gain_24;
    String code;
    String icon;
    double vol;
    Boolean isFav;
    Integer sort=0;
    BigDecimal worth;
    long openTime;


    double max_24h;
    double min_24h;
    public HomePagePriceLine(double price, float gain_24, String code, String icon, double vol) {
        this.price = price;
        this.gain_24 = gain_24;
        this.code = code;
        this.icon=icon;
        this.vol=vol;
    }

    public HomePagePriceLine() {
    }

    public HomePagePriceLine(double price, float gain_24, String code, String icon, double vol, double max_24h, double min_24h,Integer sort) {
        this.price = price;
        this.gain_24 = gain_24;
        this.code = code;
        this.icon = icon;
        this.vol = vol;
        this.max_24h = max_24h;
        this.min_24h = min_24h;
        this.sort = sort;
    }
}
