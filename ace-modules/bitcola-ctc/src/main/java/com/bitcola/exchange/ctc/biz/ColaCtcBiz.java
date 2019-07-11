package com.bitcola.exchange.ctc.biz;

import com.bitcola.ctc.ColaCtcBankCard;
import com.bitcola.ctc.ColaCtcFee;
import com.bitcola.ctc.ColaCtcOrder;
import com.bitcola.ctc.CtcOrderConstant;
import com.bitcola.exchange.ctc.feign.IPushFeign;
import com.bitcola.exchange.ctc.mapper.ColaCtcBankCardMapper;
import com.bitcola.exchange.ctc.mapper.ColaCtcFeeMapper;
import com.bitcola.exchange.ctc.mapper.ColaCtcMapper;
import com.bitcola.exchange.ctc.util.UsdtPriceUtil;
import com.bitcola.exchange.ctc.vo.BuyResponse;
import com.bitcola.exchange.ctc.vo.CtcOrderResponse;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.constant.SmsModuleConstant;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.util.Snowflake;
import com.esotericsoftware.minlog.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author zkq
 * @create 2019-05-09 10:27
 **/
@Log4j2
@Service
@Transactional
public class ColaCtcBiz {

    @Autowired
    ColaCtcMapper mapper;
    @Autowired
    ColaCtcBankCardMapper bankCardMapper;
    @Autowired
    UsdtPriceUtil priceUtil;


    @Autowired
    ColaCtcFeeMapper feeMapper;

    @Autowired
    IPushFeign pushFeign;


    Snowflake snowflake = new Snowflake();


    public List<CtcOrderResponse> list(String userID,Integer size,Long cursor,Integer isPending) {
        List<ColaCtcOrder> list = mapper.list(userID,size,cursor,null,null,null,null,isPending);
        List<CtcOrderResponse> result = new ArrayList<>();
        for (ColaCtcOrder order : list) {
            result.add(createOrderResponse(order));
        }
        return result;
    }

    /**
     * 下买单
     *      获取价格,
     *      随机一个商户,
     *      生成订单
     *      返回订单给前端
     *
     */
    public String makeBuyOrder(String userID, String coinCode, BigDecimal price,BigDecimal number, ColaCtcBankCard customerBankCard) {
        List<ColaCtcBankCard> businessList = bankCardMapper.getBusinessList();
        if (businessList.size() < 1) {
            log.error("没有可用的商户");
            return null;
        }
        ColaCtcBankCard bankCard = businessList.get(new Random().nextInt(businessList.size()));
        ColaCtcOrder ctcOrder = createCtcOrder(userID, CtcOrderConstant.BUY, coinCode, price, number, CtcOrderConstant.NOT_PAY, customerBankCard.getCardId(), bankCard.getCardId(),null);
        ctcOrder.setFee(BigDecimal.ZERO);
        mapper.insertSelective(ctcOrder);

        //buyResponse.setSuccess(true);
        //buyResponse.setCoinCode(coinCode);
        //buyResponse.setMemo(userID);
        //buyResponse.setNumber(number);
        //buyResponse.setPrice(price);
        //buyResponse.setStatus(ctcOrder.getStatus());
        //buyResponse.setPayCardId(bankCard.getCardId());
        //buyResponse.setPayBankName(bankCard.getBankName());
        //buyResponse.setPayBankAddress(bankCard.getBankAddress());
        //buyResponse.setPayUserName(bankCard.getUserName());
        //buyResponse.setPayCardIcon(bankCard.getWhiteIcon());
        // 短信通知商户
        List<String> tels = mapper.getNotifyTelephone(SmsModuleConstant.CTC_BUY);
        for (String tel : tels) {
            pushFeign.smsNotify("C2C新充值提醒: 单价:"+price+" 购买数量:"+number+" 共:¥"+price.multiply(number),tel);
        }
        return ctcOrder.getId();
    }

    public String makeSellOrder(String userID, BigDecimal sellPrice, BigDecimal number, String coinCode,ColaCtcBankCard customerBankCard) {
        ColaCtcOrder ctcOrder = createCtcOrder(userID, CtcOrderConstant.SELL, coinCode, sellPrice, number, CtcOrderConstant.NOT_PROCESSED,
                null, customerBankCard.getCardId(),CtcOrderConstant.AUDIT_NOT_PROCESSED);
        BigDecimal fee = feeMapper.selectAll().get(0).getFee();
        ctcOrder.setFee(sellPrice.multiply(number).multiply(fee));
        mapper.insertSelective(ctcOrder);
        // 短信通知商户
        List<String> tels = mapper.getNotifyTelephone(SmsModuleConstant.CTC_SELL);
        for (String tel : tels) {
            pushFeign.smsNotify("C2C新出售提醒: 单价:"+sellPrice+" 卖出数量:"+number+" 共:¥"+sellPrice.multiply(number),tel);
        }
        return ctcOrder.getId();
    }

    public BuyResponse buyDetail(ColaCtcOrder order) {
        ColaCtcBankCard bankCard = bankCardMapper.selectByPrimaryKey(order.getToCardId());
        BuyResponse response = new BuyResponse();
        if (order.getDirection().equals(CtcOrderConstant.BUY) && (order.getStatus().equals(CtcOrderConstant.NOT_PAY) || order.getStatus().equals(CtcOrderConstant.PAYED))){
            response.setPayCardId(order.getToCardId());
            response.setPayBankName(bankCard.getBankName());
            response.setPayBankAddress(bankCard.getBankAddress());
            response.setPayUserName(bankCard.getUserName());
            response.setPayCardIcon(bankCard.getWhiteIcon());
        }
        response.setCoinCode(order.getCoinCode());
        response.setMemo(order.getCustomerUserId());
        response.setNumber(order.getNumber());
        response.setPrice(order.getPrice());
        response.setStatus(order.getStatus());
        response.setFee(order.getFee());
        response.setAmount(order.getPrice().multiply(order.getNumber()).subtract(order.getFee()));
        return response;
    }


    private ColaCtcOrder createCtcOrder(String userId,String direction,String coinCode,BigDecimal price,BigDecimal number,String status,
                                        String fromCardId,String toCardId,String auditStatus){
        ColaCtcOrder order = new ColaCtcOrder();
        order.setId(snowflake.nextIdStr());
        order.setCustomerUserId(userId);
        order.setDirection(direction);
        order.setCoinCode(coinCode);
        order.setTimestamp(System.currentTimeMillis());
        order.setPrice(price);
        order.setNumber(number);
        order.setStatus(status);
        order.setFromCardId(fromCardId);
        order.setToCardId(toCardId);
        order.setAuditStatus(auditStatus);
        return order;
    }


    private CtcOrderResponse createOrderResponse(ColaCtcOrder order) {
        CtcOrderResponse response = new CtcOrderResponse();
        response.setId(order.getId());
        response.setCoinCode(order.getCoinCode());
        response.setCustomerUserId(order.getCustomerUserId());
        response.setDirection(order.getDirection());
        response.setNumber(order.getNumber());
        response.setPrice(order.getPrice());
        response.setStatus(order.getStatus());
        response.setTimestamp(order.getTimestamp());
        return response;
    }


    public boolean frozenUserBalance(String userId,String coinCode,BigDecimal number) {
        int count = mapper.frozenUserBalance(userId+coinCode,number, EncoderUtil.BALANCE_KEY);
        if (count == 1) return true;
        return false;
    }


}
