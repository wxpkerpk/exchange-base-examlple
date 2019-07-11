package com.bitcola.exchange.ctc.constant;

/**
 * @author zkq
 * @create 2019-05-08 19:31
 **/
public class CtcResponseCode {
    public static final int CTC_NO_PIN = 60101;
    public static final String CTC_NO_PIN_MSG = "未设置 Pin";

    public static final int CTC_NO_KYC = 60201;
    public static final String CTC_NO_KYC_MSG = "没有通过 KYC";

    public static final int CTC_KYC_NUMBER_ERROR = 60202;
    public static final String CTC_KYC_NUMBER_ERROR_MSG = "无法验证您的身份认证信息，请提交工单处理。提示：身份证号不是有效的 18 位身份证号";

    public static final int CTC_NO_BANK_CARD = 60301;
    public static final String CTC_NO_BANK_CARD_MSG = "未绑定银行卡";

    public static final int CTC_BANK_CARD_ERROR = 60302;
    public static final String CTC_BANK_CARD_ERROR_MSG = "绑定银行卡错误";

    public static final int CTC_HAVE_BANK_CARD_ERROR = 60303;
    public static final String CTC_HAVE_BANK_CARD_ERROR_MSG = "已经绑定银行卡，请先解绑然后再次绑定";

    public static final int CTC_NOT_PAY = 81001;
    public static final String CTC_NOT_PAY_MSG = "您有未支付的订单";

    public static final int CTC_CANCEL_MORE_THAN_THREE = 81002;
    public static final String CTC_CANCEL_MORE_THAN_THREE_MSG = "今日取消已超过 3 次";

    public static final int CTC_NUMBER_LIMIT = 81003;
    public static final String CTC_NUMBER_LIMIT_MSG = "数量不在合法范围内";

    public static final int CTC_SELL_TEN_LIMIT = 81004;
    public static final String CTC_SELL_TEN_LIMIT_MSG = "卖出超过 10 次";


}
