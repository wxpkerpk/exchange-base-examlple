package com.bitcola.dataservice.biz;

import com.bitcola.dataservice.dto.WithdrawDto;
import com.bitcola.dataservice.feign.IPushFeign;
import com.bitcola.dataservice.mapper.ColaChainMapper;
import com.bitcola.dataservice.mapper.ColaUserBalanceMapper;
import com.bitcola.dataservice.util.OrderIdUtil;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.constant.SystemBalanceConstant;
import com.bitcola.exchange.security.common.msg.ColaChainDepositResponse;
import com.bitcola.exchange.security.common.msg.ColaChainOrder;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;
import com.bitcola.me.entity.ColaCoin;
import com.bitcola.me.entity.ColaMeBalanceWithdrawin;
import com.bitcola.me.entity.ColaUserEntity;
import com.esotericsoftware.minlog.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-11-12 15:24
 **/
@Service
public class ColaChainBiz {

    @Autowired
    ColaChainMapper mapper;

    @Autowired
    ColaUserBiz userBiz;

    @Autowired
    IPushFeign pushFeign;

    @Autowired
    ColaSystemBalanceBiz balanceBiz;

    @Autowired
    ColaUserBalanceMapper balanceMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    OrderIdUtil orderIdUtil;


    public Map<String,Object> getOne(String txId){
        return mapper.getOne(txId);
    }


    public String deposit(ColaChainDepositResponse deposit) {
        // 是否开放充值
        ColaCoin coin = balanceMapper.getCoin(deposit.getCoinCode());
        if (coin.getIsRecharge() == 0) return null;
        Map<String, Object> one = getOne(deposit.getHash());
        if (one==null){
            // 判断是哪个用户
            Map<String, Object> map = mapper.info(null, deposit.getToAddress(), deposit.getCoinCode(), deposit.getMemo());
            if (map != null){
                String userId = map.get("u").toString();
                String confirm = map.get("n").toString();
                String address = map.get("address").toString();
                String icon = map.get("icon").toString();
                ColaMeBalanceWithdrawin in = new ColaMeBalanceWithdrawin();
                in.setId(orderIdUtil.getId("WithdrawDeposit"));
                in.setUserId(userId);
                in.setCoinCode(deposit.getCoinCode());
                in.setNumber(deposit.getAmount());
                in.setAddress(address);
                in.setDate(System.currentTimeMillis());
                in.setStatus("Pending");
                in.setConfirmationNumber(Integer.valueOf(confirm));
                in.setConfirmations(0);
                in.setFees(BigDecimal.ZERO);
                in.setTxId(deposit.getHash());
                in.setType("Deposit");
                in.setIcon(icon);
                in.setNote(deposit.getMemo());
                int i = mapper.newRecord(in);
                if (i == 0) throw new RuntimeException("重复插入相同 hash"+deposit.getHash());
                return in.getId();
            } else {
                return "test";
            }
        } else {
            return one.get("id").toString();
        }
    }

    public Integer confirmNumber(Integer currentConfirmNumber, String orderId) {
        return mapper.confirmNumber(currentConfirmNumber,orderId);
    }

    @Transactional
    public boolean completeDepositV2(String orderId) {
        int j = mapper.completeDepositStatus(orderId);
        if (j == 1){
            Map<String,Object> one = mapper.selectById(orderId);
            BigDecimal number = new BigDecimal(one.get("number").toString());
            String userId = one.get("user_id").toString();
            String coinCode = one.get("coin_code").toString();
            int i = mapper.completeDeposit(userId,coinCode,number,EncoderUtil.BALANCE_KEY);
            if (i == 0) throw new RuntimeException("用户资金出错:"+userId+" 币种: "+coinCode+" orderId:"+orderId);
            ColaUserEntity info = userBiz.info(userId);
            if (StringUtils.isNotBlank(info.getTelPhone())){
                pushFeign.depositSuccessSms(info.getAreaCode(),info.getTelPhone(),coinCode,number.stripTrailingZeros().toPlainString());
            } else {
                pushFeign.depositSuccessEmail(info.getEmail(),coinCode,number.stripTrailingZeros().toPlainString(),info.getLanguage(),info.getAntiPhishingCode());
            }
        }
        return true;
    }

    public List<String> getScanAddress(String module) {
        return mapper.getScanAddress(module);
    }

    public List<ColaChainOrder> getExportedOrder(String belong) {
        return mapper.getExportedOrder(belong);
    }

    @Transactional
    public void withdrawSuccess(ColaChainWithdrawResponse response, WithdrawDto order) {
        BigDecimal gas = response.getFee(); // 燃料费
        BigDecimal fee = order.getFee(); // 手续费
        int b = mapper.withdrawSuccess(order.getOrderId(), response.getHash(), EncoderUtil.WITHDRAW_KEY);
        // 扣除冻结的余额
        int i = mapper.withdrawSuccessUnFrozen(order.getUserId(),order.getNumber(),order.getCoinCode(),EncoderUtil.BALANCE_KEY);
        if (i == 1) {
            // 收取手续费
            if (fee.compareTo(BigDecimal.ZERO)>0){
                // 收取的手续费 - 区块链燃料费
                if (order.getCoinCode().equalsIgnoreCase(response.getFeeCoinCode())){
                    balanceBiz.in(order.getUserId(), fee.subtract(gas), order.getCoinCode(),
                            SystemBalanceConstant.FEES_WITHDRAW, "提币手续费,订单 ID : " + order.getOrderId());
                } else {
                    // 如果燃料费不是用户提币币种,则,系统进账收取的手续费,扣除燃料费
                    try {
                        balanceBiz.in(order.getUserId(),fee,order.getCoinCode(), SystemBalanceConstant.FEES_WITHDRAW,
                                "提币手续费,订单 ID : " + order.getOrderId());
                        if (response.getFee().compareTo(BigDecimal.ZERO)>0){
                            balanceBiz.out(order.getUserId(),gas,response.getFeeCoinCode(), SystemBalanceConstant.FEES_WITHDRAW,
                                    "扣除提币手续费,订单 ID : " + order.getOrderId());
                        }
                    } catch (Exception e) {
                        Log.warn("这里有可能去扣除 xem, 但是我们平台没有 xem 所以可能报错,这种情况下,直接跳过");
                        e.printStackTrace();
                    }
                }
            }
            // 短信或者邮件通知
            if (StringUtils.isNotBlank(order.getUserTelephone())){
                pushFeign.withdrawSuccessSms(order.getUserAreaCode(),order.getUserTelephone(),order.getCoinCode(),order.getNumber().stripTrailingZeros().toPlainString(),
                        order.getRealNumber().stripTrailingZeros().toPlainString());
            } else {
                String fishCode = order.getAntiPhishingCode()==null?"":order.getAntiPhishingCode();
                pushFeign.withdrawSuccessEmail(order.getUserEmail(),order.getCoinCode(),order.getNumber().stripTrailingZeros().toPlainString(),
                        order.getRealNumber().stripTrailingZeros().toPlainString(),order.getLanguage(),fishCode);
            }
        }
    }

    @Transactional
    public void withdrawFailed(ColaChainWithdrawResponse response, WithdrawDto order) {
        mapper.withdrawFailed(response.getOrderId(), "区块链错误:"+response.getErrMessage(),EncoderUtil.WITHDRAW_KEY);
        // 回滚用户资金
        mapper.withdrawRollback(order.getUserId(),order.getNumber(),order.getCoinCode(),EncoderUtil.BALANCE_KEY);
    }

    public WithdrawDto getWithdrawOrder(String orderId) {
        return mapper.getWithdrawOrder(orderId);
    }
}
