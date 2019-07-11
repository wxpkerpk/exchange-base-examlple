package com.bitcola.ctc;

/**
 * @author zkq
 * @create 2019-05-09 09:54
 **/
public class CtcOrderConstant {
    /**
     * 公共状态
     */
    public static final String COMPLETED = "COMPLETED"; // 完成
    public static final String CANCELED = "CANCELED"; // 取消
    public static final String FAILURE = "FAILURE"; // 失败

    /**
     * 购买状态
     */
    public static final String NOT_PAY = "NOT_PAY"; // 未付款
    public static final String PAYED = "PAYED"; // 已付款
    public static final String ARRIVED = "ARRIVED"; // 已到账

    /**
     * 卖出状态
     */
    public static final String NOT_PROCESSED = "NOT_PROCESSED"; // 未处理
    public static final String PROCESSING = "PROCESSING"; // 处理中
    public static final String EXPORTED = "EXPORTED"; // 已汇出


    /**
     * 买 or 卖
     */
    public static final String BUY = "buy"; // 买
    public static final String SELL = "sell"; // 卖

    /**
     * 系统进出
     */
    public static final String IN = "IN"; // 买
    public static final String OUT = "OUT"; // 卖

    /**
     * 审核状态
     */
    public static final String AUDIT_NOT_PROCESSED = "AUDIT_NOT_PROCESSED"; // 未处理
    public static final String AUDIT_PROCESSING = "AUDIT_PROCESSING"; // 处理中
    public static final String AUDIT_PROCESSED = "AUDIT_PROCESSED"; // 处理完成
    public static final String AUDIT_CONFIRM = "AUDIT_CONFIRM"; // 已确认放币

}
