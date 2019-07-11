package com.bitcola.exchange.constant;

public interface OrderType {
    /**
     * 限价单
     */
    String LIMIT = "LIMIT";
    /**
     * 市价单(下单只需要数量)
     */
    String MARKET = "MARKET";
    /**
     * 取消订单
     */
    String CANCEL = "CANCEL";
}
