package com.bitcola.exchange.security.me.biz;

import com.alibaba.fastjson.JSONObject;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.me.dto.BalanceDto;
import com.bitcola.exchange.security.me.feign.IChainServiceFeign;
import com.bitcola.exchange.security.me.feign.IExchangeFeign;
import com.bitcola.exchange.security.me.vo.BalanceAddressVo;
import com.bitcola.exchange.security.me.vo.BalanceItemVo;
import com.bitcola.exchange.security.me.vo.BalanceVo;
import com.bitcola.me.entity.ColaCoin;
import com.bitcola.me.entity.ColaMeBalance;
import com.bitcola.exchange.security.me.mapper.ColaCoinMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bitcola.exchange.security.me.mapper.ColaMeBalanceMapper;
import com.bitcola.exchange.security.common.biz.BaseBiz;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 用户钱包
 *
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:16
 */
@Service
public class ColaMeBalanceBiz extends BaseBiz<ColaMeBalanceMapper,ColaMeBalance> {


    @Autowired
    ColaCoinMapper coinMapper;

    @Autowired
    ColaCoinBiz colaCoinBiz;

    @Autowired
    IExchangeFeign exchangeFeign;

    @Autowired
    IChainServiceFeign chainServiceFeign;

    /**
     * 个人钱包信息
     *
     * @author zkq
     * @date 2018/7/14 16:36
     * @param userID
     * @return java.util.List<ColaUserBalanceEntity>
     */
    public BalanceVo info(String userID) {
        BalanceVo vo = new BalanceVo();
        vo.setTotal(BigDecimal.ZERO);
        List<BalanceDto> list = mapper.info(userID);
        // 补齐当前所有币种
        List<ColaCoin> colaCoinEntities = coinMapper.selectAll();
        if (list.size()!=colaCoinEntities.size()){
            this.initUserBalance(null);
            list = mapper.info(userID);
        }
        BigDecimal oldWorth = BigDecimal.ZERO;
        for (BalanceDto b : list) {
            int precision = b.getPrec();
            BigDecimal available = b.getAvailable();
            BigDecimal frozen = b.getFrozen();
            BigDecimal worth ;
            // worth
            BalanceItemVo item = new BalanceItemVo();
            item.setAllowDeposit(b.getAllowDeposit());
            item.setAllowWithdraw(b.getAllowWithdraw());
            item.setAvailable(available.setScale(precision, RoundingMode.DOWN));
            item.setFrozen(frozen.setScale(precision, RoundingMode.DOWN));
            item.setTotal(available.add(frozen).setScale(precision, RoundingMode.DOWN));
            item.setCoinCode(b.getCoinCode());
            item.setIcon(b.getIcon());
            item.setScale(b.getPrec());
            item.setIsNeedNote(b.getIsNeedNote());
            worth = available.add(frozen).multiply(colaCoinBiz.getCoinWorth(b.getCoinCode()));
            item.setWorth(worth.setScale(3, RoundingMode.DOWN));
            vo.getBalance().add(item);
            vo.setTotal(vo.getTotal().add(worth));
            // change
            BigDecimal change = BigDecimal.ZERO;
            String pair = colaCoinBiz.getPricePair(b.getCoinCode());
            if (StringUtils.isNotBlank(pair)){
                AppResponse res = exchangeFeign.getChange(pair);
                if (res.getData()!=null){
                    change = JSONObject.parseObject(JSONObject.toJSONString(res.getData())).getBigDecimal("gain_24");
                }
            }
            item.setChange(change.setScale(4,RoundingMode.DOWN));
            //  以前的钱 = 新钱 / (1+5%)
            if (BigDecimal.ONE.add(change).compareTo(BigDecimal.ZERO)!=0){
                oldWorth = oldWorth.add(worth.divide(BigDecimal.ONE.add(change),3,RoundingMode.DOWN));
            }
        }
        BigDecimal totalChange = BigDecimal.ZERO;
        if (oldWorth.compareTo(BigDecimal.ZERO) != 0){
            totalChange = vo.getTotal().subtract(oldWorth).divide(oldWorth, 4, RoundingMode.DOWN);
        }
        vo.setChange(totalChange);
        vo.setTotal(vo.getTotal().setScale(2,RoundingMode.DOWN));
        return vo;
    }

    /**
     * 获得充值地址
     *
     * @author zkq
     * @date 2018/7/14 16:36
     * @param coinCode
     * @return ColaUserBalanceEntity
     */
    public BalanceAddressVo getAddress(String coinCode) {
        ColaCoin coin = coinMapper.getByCoinCode(coinCode);
        BalanceAddressVo vo = new BalanceAddressVo();
        Integer isNeedNote = coin.getIsNeedNote();
        vo.setIsNeedNote(isNeedNote);
        vo.setCoinCode(coinCode);
        vo.setIcon(coin.getIcon());
        vo.setConfirm(coin.getDepositConfirmationNumber());
        vo.setDepositMin(coin.getDepositMin());
        ColaMeBalance obj = mapper.selectByCoinCode(coinCode,BaseContextHandler.getUserID());
        if (StringUtils.isBlank(obj.getAddressIn())){
            // 查看当前币所属系列有没有生成地址,如果有直接使用这个地址
            String address =  mapper.selectAddressAndNoteByBelong(coin.getBelong(),BaseContextHandler.getUserID());

            if (StringUtils.isBlank(address)){
                address = chainServiceFeign.newAccount(coinCode,coin.getBelong());
            }
            if (isNeedNote == 1){
                String note = BaseContextHandler.getUserID();
                vo.setNote(note);
                obj.setNote(note);
            }
            vo.setAddress(address);
            obj.setAddressIn(address);
            mapper.updateByPrimaryKey(obj);
        } else {
            vo.setAddress(obj.getAddressIn());
            vo.setNote(obj.getNote());
        }
        if (ColaLanguage.getCurrentLanguage().equals(ColaLanguage.LANGUAGE_CN)){
            vo.setDescription(coin.getRechargeDescriptionCn());
        }else {
            vo.setDescription(coin.getRechargeDescriptionEn());
        }
        return vo;
    }


    /**
     * 获取用户币种余额
     * @param userID
     * @param coinCode
     * @return
     */
    public BigDecimal getCoinNumber(String userID, String coinCode) {
        return mapper.getCoinNumber(userID,coinCode);
    }


    /**
     * 初始化用户钱包
     *
     * @author zkq
     * @date 2018/7/31 22:15
     * @param
     * @return void
     */
    public void initUserBalance(String userID){
        if (StringUtils.isBlank(userID)){
            userID = BaseContextHandler.getUserID();
        }
        List<ColaCoin> colaCoinEntities = coinMapper.selectAll();
        List<BalanceDto> info = mapper.info(userID);

        for (ColaCoin colaCoinEntity : colaCoinEntities) {
            boolean bool = true;
            for (BalanceDto entity : info) {
                String coin = entity.getCoinCode();
                if (colaCoinEntity.getCoinCode().equals(coin)){
                    bool = false;
                }
            }
            //如果用户没有当前币种钱包,则生成空钱包
            if (bool){
                String coinCode = colaCoinEntity.getCoinCode();
                ColaMeBalance entity = new ColaMeBalance();
                entity.setId(userID+coinCode);
                entity.setCoinCode(coinCode);
                entity.setUserId(userID);
                entity.setBalanceAvailable(BigDecimal.ZERO);
                entity.setBalanceFrozen(BigDecimal.ZERO);
                mapper.insertWithSign(entity, EncoderUtil.BALANCE_KEY);
            }
        }
    }

    public ColaMeBalance getColaToken(String userId) {
        return mapper.getColaToken(userId);
    }
}