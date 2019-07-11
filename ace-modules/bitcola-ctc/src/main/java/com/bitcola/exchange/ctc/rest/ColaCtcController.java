package com.bitcola.exchange.ctc.rest;

import com.bitcola.ctc.*;
import com.bitcola.exchange.ctc.biz.ColaCtcBankCardBiz;
import com.bitcola.exchange.ctc.biz.ColaCtcBiz;
import com.bitcola.exchange.ctc.biz.ColaUserBiz;
import com.bitcola.exchange.ctc.constant.CtcResponseCode;
import com.bitcola.exchange.ctc.entity.ColaUser;
import com.bitcola.exchange.ctc.feign.IDataServiceFeign;
import com.bitcola.exchange.ctc.mapper.ColaCtcFeeMapper;
import com.bitcola.exchange.ctc.mapper.ColaCtcLimitMapper;
import com.bitcola.exchange.ctc.mapper.ColaCtcMapper;
import com.bitcola.exchange.ctc.util.UsdtPriceUtil;
import com.bitcola.exchange.ctc.vo.BuyOrSellParams;
import com.bitcola.exchange.ctc.vo.BuyResponse;
import com.bitcola.exchange.ctc.vo.CtcOrderResponse;
import com.bitcola.exchange.security.auth.client.annotation.IgnoreUserToken;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppPageResponse;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.me.entity.ColaUserLimit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author zkq
 * @create 2019-05-07 18:53
 **/
@RestController
@RequestMapping("c2c")
public class ColaCtcController {

    @Autowired
    UsdtPriceUtil priceUtil;

    @Autowired
    ColaCtcLimitMapper limitMapper;

    @Autowired
    ColaCtcFeeMapper feeMapper;

    @Autowired
    ColaCtcBankCardBiz bankCardBiz;

    @Autowired
    IDataServiceFeign dataServiceFeign;

    @Autowired
    ColaUserBiz userBiz;

    @Autowired
    ColaCtcBiz biz;

    @Autowired
    ColaCtcMapper mapper;


    /**
     * 获取当前买,卖的价格
     * @return
     */
    @IgnoreUserToken
    @RequestMapping("getPrice")
    public AppResponse getPrice(String coinCode){
        if (!"USDT".equals(coinCode)) return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        BigDecimal price = priceUtil.getPrice();
        BigDecimal sellPrice = priceUtil.getSellPrice();
        Map<String,BigDecimal> map = new HashMap<>();
        map.put("buyPrice",price);
        map.put("sellPrice",sellPrice);
        return AppResponse.ok().data(map);
    }

    /**
     * 获取用户提币限制
     * 买最大最小,卖最大最小,费率
     * @return
     */
    @RequestMapping("getLimit")
    public AppResponse getLimit(String coinCode){
        if (!"USDT".equals(coinCode)) return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        String userID = BaseContextHandler.getUserID();
        ColaCtcLimit ctcLimit = limitMapper.selectByPrimaryKey(userID);
        ColaCtcFee colaCtcFee = feeMapper.selectAll().get(0);
        Map<String,Object> result = new HashMap<>();
        result.put("allowWithdraw",true);
        ColaUserLimit userLimit = dataServiceFeign.getUserLimit(userID, "withdraw");
        if (userLimit!=null){
            Long limitTime = userLimit.limitTime();
            if (limitTime>System.currentTimeMillis()){
                result.put("allowWithdraw",false);
            }
        }
        result.put("fee",colaCtcFee.getFee());
        if (ctcLimit != null){
            result.put("buyLimit",ctcLimit.getBuyLimit());
            result.put("sellLimit",ctcLimit.getSellLimit());
            result.put("buyLimitMin",ctcLimit.getBuyLimitMin());
            result.put("buyLimitMax",ctcLimit.getBuyLimitMax());
            result.put("sellLimitMin",ctcLimit.getSellLimitMin());
            result.put("sellLimitMax",ctcLimit.getSellLimitMax());
        } else {
            result.put("buyLimit",colaCtcFee.getBuyLimit());
            result.put("sellLimit",colaCtcFee.getSellLimit());
            result.put("buyLimitMin",colaCtcFee.getBuyLimitMin());
            result.put("buyLimitMax",colaCtcFee.getBuyLimitMax());
            result.put("sellLimitMin",colaCtcFee.getSellLimitMin());
            result.put("sellLimitMax",colaCtcFee.getSellLimitMax());
        }
        return AppResponse.ok().data(result);
    }

    @RequestMapping(value = "buy",method = RequestMethod.POST)
    public AppResponse buy(@RequestBody BuyOrSellParams params){
        if (StringUtils.isAnyBlank(params.getCoinCode()) || params.getNumber() == null) return AppResponse.paramsError();
        String userID = BaseContextHandler.getUserID();
        // 进行一系列认证,拥有银行卡表示有 kyc,有 pin
        List<ColaCtcBankCard> customerBankCard = bankCardBiz.list(userID);
        if (customerBankCard.size() == 0) return AppResponse.error(CtcResponseCode.CTC_NO_BANK_CARD,CtcResponseCode.CTC_NO_BANK_CARD_MSG);
        // 是否有未支付的
        List<ColaCtcOrder> orderList = mapper.list(userID,10,0L, CtcOrderConstant.BUY,CtcOrderConstant.NOT_PAY,null,null,null);
        if (orderList.size()>0) return AppResponse.error(CtcResponseCode.CTC_NOT_PAY,CtcResponseCode.CTC_NOT_PAY_MSG);
        // 是否超过限制 ,最大最小,取消 3 次
        BigDecimal price = priceUtil.getPrice();

        if (numberLimit(userID,CtcOrderConstant.BUY,price,params.getNumber())){
            return AppResponse.error(CtcResponseCode.CTC_NUMBER_LIMIT,CtcResponseCode.CTC_NUMBER_LIMIT_MSG);
        }

        // 今日取消订单是否超过 3 次
        if (userCancelOrderMoreThanThree(userID))
            return AppResponse.error(CtcResponseCode.CTC_CANCEL_MORE_THAN_THREE,CtcResponseCode.CTC_CANCEL_MORE_THAN_THREE_MSG);
        // 生成订单
        String id = biz.makeBuyOrder(userID,params.getCoinCode(),price,params.getNumber(),customerBankCard.get(0));
        if (StringUtils.isBlank(id)){
            return AppResponse.error("当前没有商户");
        }
        return AppResponse.ok().data(id);
    }

    /**
     * 和上一个接口返回一致,只能查看未完成的买单
     * @return
     */
    @RequestMapping(value = "buyDetail",method = RequestMethod.GET)
    public AppResponse buyDetail(String id){
        if (StringUtils.isBlank(id)) return AppResponse.paramsError();
        ColaCtcOrder order = mapper.selectByPrimaryKey(id);
        if (order == null) return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        BuyResponse response = biz.buyDetail(order);
        return AppResponse.ok().data(response);
    }


    /**
     * 卖出,
     * @param params
     * @return
     */
    @RequestMapping(value = "sell",method = RequestMethod.POST)
    public AppResponse sell(@RequestBody BuyOrSellParams params){
        if (StringUtils.isAnyBlank(params.getCoinCode()) || params.getNumber() == null) return AppResponse.paramsError();
        String userID = BaseContextHandler.getUserID();
        ColaUserLimit userLimit = dataServiceFeign.getUserLimit(userID, "withdraw");
        if (userLimit!=null){
            Long limitTime = userLimit.limitTime();
            if (limitTime>System.currentTimeMillis()){
                return AppResponse.error(ResponseCode.USER_LIMIT_CODE,
                        ColaLanguage.get(ColaLanguage.ME_WITHDRAW_USER_LIMIT));
            }
        }
        // 进行一系列认证,拥有银行卡表示有 kyc,有 pin
        List<ColaCtcBankCard> customerBankCard = bankCardBiz.list(userID);
        if (customerBankCard.size() == 0) return AppResponse.error(CtcResponseCode.CTC_NO_BANK_CARD,CtcResponseCode.CTC_NO_BANK_CARD_MSG);
        // pin
        ColaUser userInfo = userBiz.getUserInfo(userID);
        boolean matches = EncoderUtil.matches(params.getPin(), userInfo.getPin());
        if (!matches) return AppResponse.error(ResponseCode.PIN_ERROR_CODE,ResponseCode.PIN_ERROR_MESSAGE);
        // 是否超过限制 ,最大最小,每日10 次,取消 3 次
        BigDecimal sellPrice = priceUtil.getSellPrice();
        if (numberLimit(userID,CtcOrderConstant.SELL,sellPrice,params.getNumber())){
            return AppResponse.error(CtcResponseCode.CTC_NUMBER_LIMIT,CtcResponseCode.CTC_NUMBER_LIMIT_MSG);
        }
        if (userCancelOrderMoreThanThree(userID))
            return AppResponse.error(CtcResponseCode.CTC_CANCEL_MORE_THAN_THREE,CtcResponseCode.CTC_CANCEL_MORE_THAN_THREE_MSG);
        List<ColaCtcOrder> orderList = mapper.list(userID,10,0L, CtcOrderConstant.SELL,null,getTodayStartTimestamp(),null,null);
        if (orderList.size() >= 10) return AppResponse.error(CtcResponseCode.CTC_SELL_TEN_LIMIT,CtcResponseCode.CTC_SELL_TEN_LIMIT_MSG);
        // 余额是否足够 (冻结足够的余额)
        boolean success = biz.frozenUserBalance(userID, params.getCoinCode(), params.getNumber());
        if (!success){
            return AppResponse.error(ResponseCode.NO_ENOUGH_MONEY_CODE,ResponseCode.NO_ENOUGH_MONEY_MESSAGE);
        }
        // 生成订单
        String id = biz.makeSellOrder(userID, sellPrice, params.getNumber(), params.getCoinCode(), customerBankCard.get(0));
        return AppResponse.ok().data(id);
    }


    /**
     * 近期交易记录
     * @return
     */
    @RequestMapping("record")
    public AppPageResponse record(Long cursor,Integer isPending){
        Integer size = 10;
        if (cursor == null) cursor = 0L;
        String userID = BaseContextHandler.getUserID();
        List<CtcOrderResponse> result = biz.list(userID,size,cursor,isPending);
        Long responseCursor = null;
        if (result.size() == size){
            responseCursor = result.get(size - 1).getTimestamp();
        }
        AppPageResponse response = new AppPageResponse();
        response.setCursor(responseCursor);
        response.setData(result);
        return response;
    }


    /**
     * 已经支付
     * @return
     */
    @RequestMapping(value = "payed",method = RequestMethod.POST)
    public AppResponse payed(@RequestBody Map<String,String> params){
        String id = params.get("id");
        if (StringUtils.isBlank(id)) return AppResponse.paramsError();
        ColaCtcOrder order = mapper.selectByPrimaryKey(id);
        if (order == null) return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        if (!order.getDirection().equals(CtcOrderConstant.BUY) || !order.getStatus().equals(CtcOrderConstant.NOT_PAY)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        order.setStatus(CtcOrderConstant.PAYED);
        order.setAuditStatus(CtcOrderConstant.AUDIT_NOT_PROCESSED);
        mapper.updateByPrimaryKeySelective(order);
        return AppResponse.ok();
    }

    /**
     * 取消,只能取消未支付买单,或者未处理卖单
     * @return
     */
    @RequestMapping(value = "cancel",method = RequestMethod.POST)
    public AppResponse cancel(@RequestBody Map<String,String> params){
        String id = params.get("id");
        if (StringUtils.isBlank(id)) return AppResponse.paramsError();
        ColaCtcOrder order = mapper.selectByPrimaryKey(id);
        if (order == null) return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        if (order.getDirection().equals(CtcOrderConstant.BUY) &&
                !(order.getStatus().equals(CtcOrderConstant.NOT_PAY)||order.getStatus().equals(CtcOrderConstant.PAYED))){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        if (order.getDirection().equals(CtcOrderConstant.SELL) && !order.getStatus().equals(CtcOrderConstant.NOT_PROCESSED)){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        order.setStatus(CtcOrderConstant.CANCELED);
        mapper.updateByPrimaryKeySelective(order);
        return AppResponse.ok();
    }


    private boolean userCancelOrderMoreThanThree(String userID){
        long startTime = getTodayStartTimestamp();
        List<ColaCtcOrder> cancelOrderList = mapper.list(userID,10,0L, null,CtcOrderConstant.CANCELED,startTime,null,null);
        return cancelOrderList.size() >= 3;
    }
    private boolean numberLimit(String userID,String direction,BigDecimal price,BigDecimal number){
        BigDecimal LimitMin;
        BigDecimal LimitMax;
        ColaCtcLimit ctcLimit = limitMapper.selectByPrimaryKey(userID);
        if (ctcLimit == null) {
            ColaCtcFee colaCtcFee = feeMapper.selectAll().get(0);
            if (CtcOrderConstant.BUY.equals(direction)){
                LimitMin = colaCtcFee.getBuyLimitMin();
                LimitMax = colaCtcFee.getBuyLimitMax();
            } else {
                LimitMin = colaCtcFee.getSellLimitMin();
                LimitMax = colaCtcFee.getSellLimitMax();
            }
        } else {
            if (CtcOrderConstant.BUY.equals(direction)){
                LimitMin = ctcLimit.getBuyLimitMin();
                LimitMax = ctcLimit.getBuyLimitMax();
            } else {
                LimitMin = ctcLimit.getSellLimitMin();
                LimitMax = ctcLimit.getSellLimitMax();
            }
        }
        BigDecimal total = price.multiply(number);
        return LimitMin.compareTo(total) > 0 || LimitMax.compareTo(total) < 0;
    }

    private long getTodayStartTimestamp(){
        return System.currentTimeMillis()/(1000*3600*24)*(1000*3600*24) - TimeZone.getDefault().getRawOffset();
    }

}
