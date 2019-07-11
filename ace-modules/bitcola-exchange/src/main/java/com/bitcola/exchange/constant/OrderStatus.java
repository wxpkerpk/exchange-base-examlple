package com.bitcola.exchange.constant;

public interface OrderStatus {
    // 刚下单
    String PENDING = "PENDING";
    // 部分成交
    String PARTIAL_COMPLETED = "PARTIAL_COMPLETED";


    // 部分成交已取消
    String PARTIAL_CANCELLED = "PARTIAL_CANCELLED";
    // 全部取消
    String FULL_CANCELLED = "FULL_CANCELLED";
    // 全部成交
    String FULL_COMPLETED = "FULL_COMPLETED";

}
