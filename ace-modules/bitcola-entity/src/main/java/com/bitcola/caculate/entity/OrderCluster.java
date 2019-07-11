package com.bitcola.caculate.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/*
 * @author:wx
 * @description:订单族
 * @create:2018-07-31  20:20
 */
public class OrderCluster implements Serializable {
    List<ColaOrder>buys=new LinkedList<>();
    List<ColaOrder>sells=new LinkedList<>();
    String code;

    BigDecimal lastPrice=new BigDecimal(0);

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    public List<ColaOrder> getBuys() {
        return buys;
    }

    public void setBuys(List<ColaOrder> buys) {
        this.buys = buys;
    }

    public List<ColaOrder> getSells() {
        return sells;
    }

    public void setSells(List<ColaOrder> sells) {
        this.sells = sells;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
