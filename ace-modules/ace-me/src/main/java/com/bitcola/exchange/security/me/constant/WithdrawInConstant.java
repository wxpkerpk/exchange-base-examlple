package com.bitcola.exchange.security.me.constant;

/**
 * 充值，提币常量
 *
 * @author zkq
 * @create 2018-07-14 16:58
 **/
public class WithdrawInConstant {

    /**
     * 冲
     */
    public static final String TYPE_IN = "Deposit";
    /**
     * 提
     */
    public static final String TYPE_WITHDRAW = "Withdraw";
    /**
     * 审核中
     */
    public static final String STATUS_CHECKING = "Checking";
    /**
     * 审核通过
     */
    public static final String STATUS_CHECK_SUCCESS = "Checked";
    /**
     * 已汇出
     */
    public static final String STATUS_WITHDRAW = "Exported";
    /**
     * 确认中
     */
    public static final String STATUS_CONFIRM = "Pending";
    /**
     * 确认成功
     */
    public static final String STATUS_SUCCESS = "Completed";
    /**
     * 提币拒绝
     */
    public static final String STATUS_ERROR= "Refuse";
    /**
     *  Failed
     */
    public static final String STATUS_FAILED= "Failed";
}
