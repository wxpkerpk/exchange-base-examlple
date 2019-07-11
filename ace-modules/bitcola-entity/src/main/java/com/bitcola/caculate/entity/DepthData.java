package com.bitcola.caculate.entity;

import java.io.Serializable;

/*
 * @author:wx
 * @description:交易深度
 * @create:2018-08-11  21:22
 */
public class DepthData implements Serializable {
   public Number[][] ask;
    public Number[][]bids;

    public Number[][] getAsk() {
        if (ask == null){
            ask = new Number[][]{};
        }
        return ask;
    }

    public void setAsk(Number[][] ask) {
        this.ask = ask;
    }

    public Number[][] getBids() {
        if (bids == null){
            bids = new Number[][]{};
        }
        return bids;
    }

    public void setBids(Number[][] bids) {
        this.bids = bids;
    }
}
