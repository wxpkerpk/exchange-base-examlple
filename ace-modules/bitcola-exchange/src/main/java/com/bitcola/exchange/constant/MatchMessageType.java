package com.bitcola.exchange.constant;

public interface MatchMessageType {
    /**
     * 正常撮合结算
     */
    String MATCH_RESULT = "MATCH_RESULT";
    /**
     * 取消订单结算
     */
    String MATCH_CANCEL = "MATCH_CANCEL";
}
