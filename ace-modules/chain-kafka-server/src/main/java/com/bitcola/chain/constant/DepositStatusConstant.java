package com.bitcola.chain.constant;

/**
 * @author zkq
 * @create 2019-01-22 12:11
 **/
public class DepositStatusConstant {
    // 未记录,刚扫描到
    public static final String NOT_RECORD = "NOT_RECORD";
    // 未确认,已经记录 orderId
    public static final String NOT_CONFIRM = "NOT_CONFIRM";
    // 已经确认,这笔订单已经完成了
    public static final String CONFIRM = "CONFIRM";

}
