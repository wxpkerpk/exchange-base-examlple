package com.bitcola.exchange.security.admin.biz;

import com.bitcola.ctc.ColaCtcBankCard;
import com.bitcola.ctc.ColaCtcFee;
import com.bitcola.ctc.ColaCtcOrder;
import com.bitcola.ctc.CtcOrderConstant;
import com.bitcola.exchange.security.admin.entity.ColaCtcBusinessCard;
import com.bitcola.exchange.security.admin.entity.ColaCtcOrderBull;
import com.bitcola.exchange.security.admin.feign.IPushFeign;
import com.bitcola.exchange.security.admin.mapper.ColaCtcBusinessCardMapper;
import com.bitcola.exchange.security.admin.mapper.ColaCtcFeeMapper;
import com.bitcola.exchange.security.admin.mapper.ColaCtcOrderBullMapper;
import com.bitcola.exchange.security.admin.mapper.ColaCtcOrderMapper;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.constant.UserConstant;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.AdminQuery;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author zkq
 * @create 2019-05-10 11:18
 **/
@Log4j2
@Service
@Transactional
public class ColaCtcBiz {

    @Autowired
    ColaCtcOrderMapper mapper;

    @Autowired
    ColaCtcBankCardBiz bankCardBiz;

    @Autowired
    ColaCtcBusinessCardMapper businessCardMapper;

    @Autowired
    ColaCtcOrderBullMapper orderBullMapper;

    @Autowired
    ColaCtcFeeMapper feeMapper;

    @Autowired
    IPushFeign pushFeign;

    public TableResultResponse page(AdminQuery query) {
        query.put("userId", BaseContextHandler.getUserID());
        List<ColaCtcOrder> list = mapper.list(query);
        Long count = mapper.count(query);
        return new TableResultResponse(count,list);
    }

    public boolean acceptTask(String orderId) {
        ColaCtcOrder ctcOrder = mapper.selectByPrimaryKey(orderId);
        if (CtcOrderConstant.AUDIT_NOT_PROCESSED.equals(ctcOrder.getAuditStatus())){
            ctcOrder.setAuditor(BaseContextHandler.getUserID());
            ctcOrder.setAuditStatus(CtcOrderConstant.AUDIT_PROCESSING);
            ctcOrder.setAuditTimestamp(System.currentTimeMillis());
            if (ctcOrder.getDirection().equals(CtcOrderConstant.SELL)){
                ctcOrder.setStatus(CtcOrderConstant.PROCESSING);
            }
            mapper.updateByPrimaryKeySelective(ctcOrder);
            return true;
        }
        return false;
    }

    public String getPin(String userId){
        return mapper.getPin(userId);
    }

    public void confirmAsset(String orderId, String bankSerialNumber, BigDecimal amount) {
        ColaCtcOrder ctcOrder = mapper.selectByPrimaryKey(orderId);
        if (!ctcOrder.getAuditor().equals(BaseContextHandler.getUserID())){
            return;
        }
        if (!ctcOrder.getAuditStatus().equals(CtcOrderConstant.AUDIT_PROCESSING)) return;
        String toCustomer = bankCardBiz.selectById(ctcOrder.getToCardId()).getUserName();
        String fromCustomer = bankCardBiz.selectById(ctcOrder.getFromCardId()).getUserName();
        ColaCtcOrderBull orderBull = new ColaCtcOrderBull();
        orderBull.setId(UUID.randomUUID().toString());
        orderBull.setOrderId(orderId);
        orderBull.setAmount(amount);
        orderBull.setBankSerialNumber(bankSerialNumber);
        orderBull.setFromCardId(ctcOrder.getFromCardId());
        orderBull.setToCardId(ctcOrder.getToCardId());
        orderBull.setFromCustomer(fromCustomer);
        orderBull.setToCustomer(toCustomer);
        if (ctcOrder.getDirection().equals(CtcOrderConstant.BUY)){
            orderBull.setType(CtcOrderConstant.IN);
            ctcOrder.setStatus(CtcOrderConstant.ARRIVED);
        } else {
            orderBull.setType(CtcOrderConstant.OUT);
            ctcOrder.setStatus(CtcOrderConstant.EXPORTED);
        }
        orderBull.setTimestamp(System.currentTimeMillis());
        orderBullMapper.insertSelective(orderBull);
        ctcOrder.setAuditStatus(CtcOrderConstant.AUDIT_PROCESSED);
        ctcOrder.setAuditTimestamp(System.currentTimeMillis());
        mapper.updateByPrimaryKeySelective(ctcOrder);
    }

    /**
     * 最终确认人
     *      1 结束订单
     *      2 发放资金,或者扣除冻结资金
     *          短信通知客户已经处理完成
     *      3 修改商户余额
     *      4 短信通知
     *
     * @param orderId
     */
    public void confirm(String orderId) {
        ColaCtcOrder ctcOrder = mapper.selectByPrimaryKey(orderId);
        if (!ctcOrder.getAuditStatus().equals(CtcOrderConstant.AUDIT_PROCESSED)) return;

        ctcOrder.setStatus(CtcOrderConstant.COMPLETED);
        ctcOrder.setAuditStatus(CtcOrderConstant.AUDIT_CONFIRM);
        ctcOrder.setConfirmUserId(BaseContextHandler.getUserID());
        ctcOrder.setConfirmTimestamp(System.currentTimeMillis());
        mapper.updateByPrimaryKeySelective(ctcOrder);

        String telephone = mapper.getTelephone(ctcOrder.getCustomerUserId());
        BigDecimal amount = getBullAmount(ctcOrder.getId());
        if (ctcOrder.getDirection().equals(CtcOrderConstant.BUY)){
            buy(UserConstant.SYS_CTC_ID,ctcOrder.getCustomerUserId(),ctcOrder.getCoinCode(),ctcOrder.getNumber());
            String toCardId = ctcOrder.getToCardId();
            businessBalance(toCardId,amount);
            pushFeign.smsNotify("您的购买的 "+ctcOrder.getNumber().stripTrailingZeros().toPlainString()+" USDT 已经到账，请登录 BitCola 查收。",telephone);
        } else {
            sell(ctcOrder.getCustomerUserId(),UserConstant.SYS_CTC_ID,ctcOrder.getCoinCode(),ctcOrder.getNumber());
            String fromCardId = ctcOrder.getFromCardId();
            businessBalance(fromCardId,amount.negate());
            pushFeign.smsNotify("您的出售的 "+ctcOrder.getNumber().stripTrailingZeros().toPlainString()+" USDT 已处理完成，请查看您绑定的银行账户。",telephone);
        }

    }

    /**
     * 商户加钱或者扣钱
     * @param cardId
     * @param number 扣钱为负
     */
    private void businessBalance(String cardId,BigDecimal number){
        ColaCtcBusinessCard businessCard = businessCardMapper.selectByPrimaryKey(cardId);
        businessCard.setBalance(businessCard.getBalance().add(number));
        businessCardMapper.updateByPrimaryKey(businessCard);
    }

    /**
     * 扣除掉用户的冻结资金
     */
    private void sell(String user, String system, String coinCode, BigDecimal number) {
        int count = mapper.sellSub(user+coinCode,number,EncoderUtil.BALANCE_KEY);
        if (count == 1) {
            count = mapper.sellAdd(system+coinCode,number,EncoderUtil.BALANCE_KEY);
            if (count == 1) return;
        }
        log.error("资金出错");
        throw new RuntimeException("资金出错");
    }


    private void buy(String system,String toUser,String coinCode,BigDecimal number){
        int count = mapper.buySub(system+coinCode,number, EncoderUtil.BALANCE_KEY);
        if (count == 1){
            count = mapper.buyAdd(toUser+coinCode,number, EncoderUtil.BALANCE_KEY);
            if (count == 1) return;
        }
        log.error("资金出错");
        throw new RuntimeException("资金出错");
    }

    private BigDecimal getBullAmount(String orderId){
        List<ColaCtcOrderBull> bulls = getBullByOrderId(orderId);
        BigDecimal amount = BigDecimal.ZERO;
        for (ColaCtcOrderBull orderBull : bulls) {
            amount.add(orderBull.getAmount());
        }
        return amount;
    }
    private List<ColaCtcOrderBull> getBullByOrderId(String orderId){
        ColaCtcOrderBull bull = new ColaCtcOrderBull();
        bull.setOrderId(orderId);
        Example example = new Example(bull.getClass());
        return orderBullMapper.selectByExample(example);
    }



    public boolean refuse(String orderId, Boolean backFrozen) {
        ColaCtcOrder ctcOrder = mapper.selectByPrimaryKey(orderId);
        if (ctcOrder.getAuditStatus().equals(CtcOrderConstant.AUDIT_PROCESSED)
                || ctcOrder.getAuditStatus().equals(CtcOrderConstant.AUDIT_CONFIRM))
            return false;
        ctcOrder.setStatus(CtcOrderConstant.FAILURE);
        ctcOrder.setAuditStatus(CtcOrderConstant.FAILURE);
        mapper.updateByPrimaryKeySelective(ctcOrder);
        if (ctcOrder.getDirection().equals(CtcOrderConstant.SELL) && backFrozen != null && backFrozen){
            int count = mapper.unFrozen(ctcOrder.getCustomerUserId()+ctcOrder.getCoinCode(),ctcOrder.getNumber(),EncoderUtil.BALANCE_KEY);
            if (count == 1) return true;
            throw new RuntimeException(("资金出错"));
        }
        return true;
    }

    public ColaCtcFee getFeeLimit() {
        return feeMapper.selectAll().get(0);
    }

    public void updateFeeLimit(ColaCtcFee fee) {
        feeMapper.updateByPrimaryKeySelective(fee);
    }

    /**
     * 详情
     *
     * @param id
     * @return
     */
    public Map<String, Object> detail(String id) {
        ColaCtcOrder order = mapper.selectByPrimaryKey(id);
        ColaCtcBankCard fromCard = bankCardBiz.selectById(order.getFromCardId());
        ColaCtcBankCard toCard = bankCardBiz.selectById(order.getToCardId());
        List<ColaCtcOrderBull> bulls = getBullByOrderId(id);
        Map<String,Object> map = new HashMap<>();
        map.put("order",order);
        map.put("fromCard",fromCard);
        map.put("toCard",toCard);
        map.put("bulls",bulls);
        return map;
    }
}
