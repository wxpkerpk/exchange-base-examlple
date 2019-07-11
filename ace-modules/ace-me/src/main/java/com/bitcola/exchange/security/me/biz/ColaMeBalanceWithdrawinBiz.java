package com.bitcola.exchange.security.me.biz;

import com.bitcola.caculate.entity.ExchangeLog;
import com.bitcola.chaindata.entity.WithdrawResponse;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.biz.BaseBiz;
import com.bitcola.exchange.security.common.constant.FinancialConstant;
import com.bitcola.exchange.security.common.constant.SystemBalanceConstant;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.ColaChainBalance;
import com.bitcola.exchange.security.common.msg.ColaChainWithdrawResponse;
import com.bitcola.exchange.security.common.util.MD5Utils;
import com.bitcola.exchange.security.common.util.TimeUtils;
import com.bitcola.exchange.security.me.constant.WithdrawInConstant;
import com.bitcola.exchange.security.me.dto.WithdrawDto;
import com.bitcola.exchange.security.me.feign.IChainServiceFeign;
import com.bitcola.exchange.security.me.feign.IConfigFeign;
import com.bitcola.exchange.security.me.feign.IDataServiceFeign;
import com.bitcola.exchange.security.me.feign.IPushFeign;
import com.bitcola.exchange.security.me.mapper.ColaFinancialRecordsMapper;
import com.bitcola.exchange.security.me.mapper.ColaMeBalanceMapper;
import com.bitcola.exchange.security.me.util.DateUtil;
import com.bitcola.exchange.security.me.util.OrderIdUtil;
import com.bitcola.exchange.security.me.vo.BalanceVo;
import com.bitcola.exchange.security.me.vo.InWithdrawDetail;
import com.bitcola.exchange.security.me.vo.InWithdrawListVo;
import com.bitcola.me.entity.ColaAbnormalEntity;
import com.bitcola.me.entity.ColaCoin;
import com.bitcola.me.entity.ColaUserEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.bitcola.me.entity.ColaMeBalanceWithdrawin;
import com.bitcola.exchange.security.me.mapper.ColaMeBalanceWithdrawinMapper;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 用户提现记录
 *
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:16
 */
@Service
public class ColaMeBalanceWithdrawinBiz extends BaseBiz<ColaMeBalanceWithdrawinMapper,ColaMeBalanceWithdrawin> {

    @Autowired
    ColaMeBalanceWithdrawinMapper mapper;

    @Autowired
    ColaCoinBiz coinBiz;

    @Autowired
    ColaMeBalanceBiz userBalanceBiz;

    @Autowired
    ColaMeBalanceMapper balanceMapper;

    @Autowired
    ColaUserBiz userBiz;

    @Autowired
    ColaCoinBiz colaCoinBiz;

    @Autowired
    ColaFinancialRecordsMapper financialRecordsMapper;

    @Autowired
    IPushFeign pushFeign;

    @Autowired
    OrderIdUtil orderIdUtil;

    @Autowired
    IChainServiceFeign chainServiceFeign;

    @Autowired
    IDataServiceFeign dataServiceFeign;

    @Autowired
    IConfigFeign configFeign;

    @Autowired
    ColaMeBalanceWithdrawinBiz biz;

    @Autowired
    RedisTemplate<String,String> redisTemplate;

    /**
     * 冲提记录
     * @param coinCode
     * @param timestamp
     * @param keyWord
     * @return
     */
    public List<InWithdrawListVo> list(String coinCode, Long timestamp, Integer size, String keyWord,Long startTime,Long endTime,String type) {
        List<InWithdrawListVo> voList = new ArrayList<>();
        ColaCoin byCoinCode = colaCoinBiz.getByCoinCode(coinCode);
        List<ColaMeBalanceWithdrawin> list = mapper.list(coinCode, BaseContextHandler.getUserID(), timestamp, size,keyWord,startTime,endTime,type);
        for (ColaMeBalanceWithdrawin balance : list) {
            InWithdrawListVo vo = new InWithdrawListVo();
            vo.setId(balance.getId());
            vo.setCoinCode(balance.getCoinCode());
            vo.setTime(balance.getDate());
            vo.setNumber(balance.getNumber());
            if(WithdrawInConstant.TYPE_IN.equalsIgnoreCase(balance.getType())){
                vo.setConfirmation(balance.getConfirmations().toString()+"/"+balance.getConfirmationNumber());
            }
            vo.setTxId(balance.getTxId());
            vo.setStatus(balance.getStatus());
            vo.setType(balance.getType());
            vo.setIcon(balance.getIcon());
            vo.setScale(byCoinCode.getPrec());
            voList.add(vo);
        }
        return voList;
    }


    /**
     * 获取用户当日提现额度
     * @param userID
     * @param coinCode
     * @return
     */
    public BigDecimal getTodayNumber(String userID, String coinCode) {
        return  mapper.getTodayNumber(userID, coinCode, DateUtil.getTodyTime());
    }

    /**
     * 获取当日提现次数
     * @param userID
     * @return
     */
    public int getTodayTime(String userID) {
        return mapper.getTodayTime(userID, DateUtil.getTodyTime());
    }

    /**
     * 提现信息
     * @param coinCode
     * @param userID
     * @return
     */
    public Map<String, Object> withdrawInfo(String coinCode, String userID) {
        ColaUserEntity info = userBiz.info(BaseContextHandler.getUserID());
        String telPhone = info.getTelPhone();

        Map<String, Object> map = new HashMap<>();
        ColaCoin coin = coinBiz.getByCoinCode(coinCode);
        map.put("withdrawOne",coin.getWithdrawOne());
        map.put("withdrawAmount",coin.getWithdrawAmount());
        map.put("feesFlat",coin.getFeesFlat());
        map.put("feesPercent",coin.getFeesPercent());
        map.put("minWithdrawNumber",coin.getMinWithdrawNumber());
        BigDecimal number = userBalanceBiz.getCoinNumber(userID,coinCode);
        map.put("balanceAvailable",number.setScale(coin.getPrec(), RoundingMode.DOWN));
        map.put("dailyLimitNumber",info.getWithdrawTime());
        map.put("pinSet",StringUtils.isNotBlank(info.getMoneyPassword()));
        if (StringUtils.isBlank(telPhone)){
            map.put("mode","GoogleCode");
            map.put("googleCodeSet",StringUtils.isNotBlank(info.getGoogleSecretKey()));
        } else {
            map.put("mode","SMSCode");
            map.put("googleCodeSet",StringUtils.isNotBlank(info.getGoogleSecretKey()));
        }
        return map;
    }


    public InWithdrawDetail detail(String orderId) {
        InWithdrawDetail detail = mapper.detail(orderId);
        if (StringUtils.isNotBlank(detail.getTxId())){
            detail.setTxId(detail.getTxId().split("@")[0]);
        }
        BigDecimal worth = colaCoinBiz.getCoinWorth(detail.getCoinCode()).multiply(detail.getNumber());
        detail.setWorth(worth);
        return detail;
    }

    /**
     * 提币申请
     * @param userID
     * @param coinCode
     * @param id
     */
    public void withdrawApply(String userID, String coinCode, String address, BigDecimal number, ColaCoin coin, String note, String reason, String id) {
        perWithdraw(userID, coinCode, address, number, coin, note, reason,WithdrawInConstant.STATUS_CHECKING,id);
    }


    private void perWithdraw(String userID, String coinCode, String address, BigDecimal number, ColaCoin coin, String note, String reason,String status,String id) {
        //扣除手续费
        BigDecimal feesFlat = coin.getFeesFlat();
        BigDecimal feesPercent = coin.getFeesPercent();

        BigDecimal fee = feesFlat;
        if (feesPercent.compareTo(BigDecimal.ZERO) != 0){
            fee = fee.add(number.multiply(feesPercent));
        }
        BigDecimal realNumber = number.subtract(fee);
        ColaMeBalanceWithdrawin entity = new ColaMeBalanceWithdrawin();
        entity.setAddress(address);
        entity.setId(id);
        entity.setUserId(userID);
        entity.setCoinCode(coinCode);
        entity.setDate(System.currentTimeMillis());
        entity.setIcon(coin.getIcon());
        entity.setNote(note);
        entity.setNumber(number);
        entity.setType(WithdrawInConstant.TYPE_WITHDRAW);
        entity.setRealNumber(realNumber);
        entity.setStatus(status);
        entity.setReason(reason);
        entity.setFees(fee);
        entity.setSign(MD5Utils.MD5(EncoderUtil.WITHDRAW_KEY+id+userID+status+number.setScale(0,RoundingMode.DOWN)));
        int i = mapper.perWithdraw(userID,number,coinCode,EncoderUtil.BALANCE_KEY);
        if (i==1){
            mapper.insert(entity);
        }


    }




    public WithdrawResponse checkBalance(String coinCode, BigDecimal number) {
        WithdrawResponse response = new WithdrawResponse();
        String userID = BaseContextHandler.getUserID();
        Integer unNormal = mapper.checkBalance(userID,EncoderUtil.BALANCE_KEY);
        if (unNormal == 0){
            List<Map<String,Object>> listFinancial = mapper.listFinancial(BaseContextHandler.getUserID());
            BigDecimal deposit = BigDecimal.ZERO; // 充值总额
            BigDecimal withdraw = BigDecimal.ZERO; // 提币总额
            for (Map<String, Object> map : listFinancial) {
                String coin = map.get("coin").toString();
                BigDecimal account = new BigDecimal(map.get("account").toString());
                BigDecimal worth = coinBiz.getCoinWorth(coin);
                if (map.get("action").toString().equals(FinancialConstant.WITHDRAW)){
                    withdraw = withdraw.add(account.multiply(worth));
                } else if (map.get("action").toString().equals(FinancialConstant.DEPOSIT)){
                    deposit = deposit.add(account.multiply(worth));
                }
            }
            BigDecimal total = coinBiz.getCoinWorth(coinCode).multiply(number);
            if (BigDecimal.ZERO.compareTo(deposit)==0){
                if (total.add(withdraw).compareTo(new BigDecimal(200))>0){
                    response.setChecked(false);
                    response.setReason("没有充值,提币超过200美元");
                    return response;
                }
            } else {
                String config = configFeign.getConfig("cola_withdraw_deposit_rate");
                int i = withdraw.add(total).divide(deposit,10,RoundingMode.HALF_UP).compareTo(new BigDecimal(config));

                if (i>0){
                    response.setChecked(false);
                    response.setReason("提币数量超过充值数量"+config+"倍");
                    return response;
                }
            }
            ColaChainBalance balance = mapper.getChainBalanceByCoinCode(coinCode);
            if (number.add(balance.getFeeLimit()).compareTo(balance.getBalance()) > 0 || balance.getFeeLimit().compareTo(balance.getFeeBalance()) > 0){
                response.setChecked(false);
                response.setReason(coinCode+" 资金或者燃料费不足,当前资金:"+balance.getBalance().stripTrailingZeros().toPlainString()+
                        " 燃料费:"+balance.getFeeBalance().stripTrailingZeros().toPlainString());
                return response;
            }
            balance.setBalance(balance.getBalance().subtract(number));
            mapper.updateChainBalanceById(balance);
            response.setChecked(true);
        } else {
            response.setChecked(false);
            response.setReason("资金签名出错");
        }
        return response;
    }

    public String withdraw( String id,String coinCode) {
        mapper.checkSuccess(id,WithdrawInConstant.STATUS_WITHDRAW,EncoderUtil.WITHDRAW_KEY);
        return "可以了";
    }

    private static final String GET_CHAIN_BALANCE_LIMIT = "GET_CHAIN_BALANCE_LIMIT";

    /**
     * 获取区块链余额
     */
    @Scheduled(cron = "0 0 */1 * * ?")
    public void getChainBalance(){
        String key = GET_CHAIN_BALANCE_LIMIT+ TimeUtils.getDateFormat(System.currentTimeMillis());
        Boolean requestTag= redisTemplate.opsForValue().setIfAbsent(key ,"1");
        if (requestTag){
            try {
                List<ColaChainBalance> allChainBalance = mapper.getAllChainBalance();
                for (ColaChainBalance balance : allChainBalance) {
                    getChainBalance(balance);
                }
                Thread.sleep(60 * 1000);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                redisTemplate.delete(key);
            }
        }
    }

    public void getChainBalance(ColaChainBalance balance){
        ColaChainBalance response = chainServiceFeign.getChainBalance(balance.getModule(),balance.getCoinCode(),balance.getFeeCoinCode());
        mapper.updateChainBalanceById(response);
    }


}