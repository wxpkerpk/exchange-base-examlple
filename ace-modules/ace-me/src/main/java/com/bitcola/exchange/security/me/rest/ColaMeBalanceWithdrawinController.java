package com.bitcola.exchange.security.me.rest;


import com.bitcola.chaindata.entity.WithdrawResponse;
import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.auth.common.util.EncoderUtil;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.me.biz.*;
import com.bitcola.exchange.security.me.constant.EmailCaptchaConstant;
import com.bitcola.exchange.security.me.constant.TrueFalseConstant;
import com.bitcola.exchange.security.me.feign.IChainServiceFeign;
import com.bitcola.exchange.security.me.feign.IChatFeign;
import com.bitcola.exchange.security.me.feign.IDataServiceFeign;
import com.bitcola.exchange.security.me.feign.IPushFeign;
import com.bitcola.exchange.security.me.util.GoogleAuthenticator;
import com.bitcola.exchange.security.me.util.OrderIdUtil;
import com.bitcola.exchange.security.me.util.RedisUtil;
import com.bitcola.exchange.security.me.util.SequenceFactory;
import com.bitcola.exchange.security.me.vo.InWithdrawDetail;
import com.bitcola.exchange.security.me.vo.InWithdrawListVo;
import com.bitcola.me.entity.ColaCoin;
import com.bitcola.me.entity.ColaMeBalanceWithdrawin;
import com.bitcola.me.entity.ColaUserEntity;
import com.bitcola.me.entity.ColaUserLimit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 冲币,提币
 * 
 * @author zkq
 * @date 2018/8/2 16:11
 */
@RestController
@RequestMapping("colaMeBalanceWithdrawin")
public class ColaMeBalanceWithdrawinController{

    @Value("${spring.redis.host}")
    String redisHost;

    @Autowired
    ColaMeBalanceWithdrawAddressBiz addressBiz;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ColaMeBalanceWithdrawinBiz biz;

    @Autowired
    ColaCoinBiz coinBiz;

    @Autowired
    ColaUserBiz userBiz;

    @Autowired
    OrderIdUtil orderIdUtil;

    @Autowired
    ColaMeBalanceBiz userBalanceBiz;

    @Autowired
    SequenceFactory sequenceFactory;

    @Autowired
    IDataServiceFeign dataServiceFeign;

    @Autowired
    IPushFeign pushFeign;

    @Autowired
    IChatFeign chatFeign;

    ExecutorService executor = Executors.newFixedThreadPool(2);

    /**
     * 提币  (通过之后  短信通知,或者邮箱通知)
     * @return
     */
    @RequestMapping( value = "withdraw",method = RequestMethod.POST)
    public AppResponse withdraw(@RequestBody Map<String,String> params){
        String coinCode = params.get("coinCode");
        String address = params.get("address");
        String note = params.get("note") == null?"":params.get("note");
        BigDecimal number = new BigDecimal(params.get("number"));
        String moneyPassword = params.get("transactionPin");
        String captcha = params.get("captcha"); // 谷歌验证码或者是短信验证码
        String userID = BaseContextHandler.getUserID();
        if ("120.79.250.164".equalsIgnoreCase(redisHost)){
            List<String> ids = new ArrayList<>();
            ids.add("101");
            ids.add("111");
            if (!ids.contains(userID)){
                return AppResponse.error(ColaLanguage.get(ColaLanguage.FORBIDDEN));
            }
        }
        ColaUserLimit userLimit = dataServiceFeign.getUserLimit(BaseContextHandler.getUserID(), "withdraw");
        if (userLimit!=null){
            Long limitTime = userLimit.limitTime();
            if (limitTime>System.currentTimeMillis()){
                return AppResponse.error(ResponseCode.USER_LIMIT_CODE,
                        ColaLanguage.get(ColaLanguage.ME_WITHDRAW_USER_LIMIT));
            }
        }
        if (StringUtils.isBlank(coinCode) || StringUtils.isBlank(address) || StringUtils.isBlank(moneyPassword) ||    captcha == null) {
            return AppResponse.paramsError();
        }
        //查看是否可提币
        ColaCoin coin = coinBiz.getByCoinCode(coinCode);
        if (coin.getIsWithdraw() == TrueFalseConstant.BOOLEAN_NUMBER_FALSE){
            return new AppResponse(ResponseCode.COIN_NOT_WITHDRAW_CODE,ResponseCode.COIN_NOT_WITHDRAW_MESSAGE);
        }
        ColaUserEntity info = userBiz.info(userID);
        if (StringUtils.isBlank(info.getMoneyPassword())){
            return new AppResponse(ResponseCode.NO_MONEY_PASSWORD_CODE, ResponseCode.NO_MONEY_PASSWORD_MESSAGE);
        }
        long i = sequenceFactory.generate("withdraw_pin_limit"+BaseContextHandler.getUserID(),2, TimeUnit.HOURS);
        if (i>5){
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_LOGIN_ERROR_LIMIT));
        }
        boolean b = EncoderUtil.matches(moneyPassword,info.getMoneyPassword());
        if (!b){
            return new AppResponse(ResponseCode.PIN_ERROR_CODE,ResponseCode.PIN_ERROR_MESSAGE);
        }
        sequenceFactory.delete("withdraw_pin_limit"+BaseContextHandler.getUserID());
        //判断验证码
        if (StringUtils.isBlank(info.getTelPhone())){
            //验证谷歌验证码
            if (!GoogleAuthenticator.check_code(info.getGoogleSecretKey(),Long.valueOf(captcha),System.currentTimeMillis())){
                return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_COMMON_CAPTCHA_ERROR));
            }
        } else {
            //手机验证码
            Object cap = redisUtil.get(EmailCaptchaConstant.SMS_CAPTCHA +info.getAreaCode()+info.getTelPhone());
            if (!captcha.equals(cap)){
                return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_COMMON_CAPTCHA_ERROR));
            } else {
                redisUtil.remove(EmailCaptchaConstant.SMS_CAPTCHA +info.getAreaCode()+info.getTelPhone());
            }
        }

        // todo 3，判断当前提币地址是否为当前币种，如果不是，这返回前端地址错误

        BigDecimal remain = userBalanceBiz.getCoinNumber(BaseContextHandler.getUserID(), coinCode);
        if (remain.compareTo(number)<0){
            return new AppResponse(ResponseCode.TIP_ERROR_CODE,ColaLanguage.get(ColaLanguage.ME_BALANCE_NOT_ENOUGH));
        }

        // 单次最大最小限额
        if (coin.getMinWithdrawNumber().compareTo(number)>0){
            return new AppResponse(ResponseCode.TIP_ERROR_CODE,ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }

        BigDecimal withdrawOne = coin.getWithdrawOne();
        if(withdrawOne.compareTo(number) < 0){
            return new AppResponse(ResponseCode.TIP_ERROR_CODE,ColaLanguage.get(ColaLanguage.FORBIDDEN));
        }
        // 4，判断当日限额和全部限额是否已满
        BigDecimal withdrawAmount = coin.getWithdrawAmount();
        BigDecimal todayNumber = biz.getTodayNumber(userID,coinCode);
        if (withdrawAmount.compareTo(todayNumber.add(number)) < 0){
            return new AppResponse(ResponseCode.TIP_ERROR_CODE,ColaLanguage.get(ColaLanguage.ME_WITHDRAW_DALLY_NUMBER_LIMIT));
        }
        // 当日限制提现次数
        int time =  biz.getTodayTime(userID);
        int totalTime = info.getWithdrawTime();
        if (time >= totalTime){
            return new AppResponse(ResponseCode.TIP_ERROR_CODE,ColaLanguage.get(ColaLanguage.ME_WITHDRAW_DALLY_LIMIT));
        }

        // 保存地址
        String tag = params.get("tag");
        if (StringUtils.isNotBlank(tag)){
            addressBiz.add(tag,address,note,coinCode);
        }

        String id = orderIdUtil.getId("WithdrawDeposit");
        biz.withdrawApply(userID,coinCode, address,number,coin,note,null,id);
        // 验证当前用户资金是否异常
        WithdrawResponse normal = biz.checkBalance(coinCode,number);
        if (normal.isChecked()){
            //正在自动提现
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    biz.withdraw(id,coinCode);
                }
            });
        } else {
            ColaMeBalanceWithdrawin withdrawin = new ColaMeBalanceWithdrawin();
            withdrawin.setAuditReason(normal.getReason());
            withdrawin.setId(id);
            biz.updateSelectiveById(withdrawin);
            chatFeign.sendChatMessage("200011","1","有一笔提币进入审核,原因是:"+normal.getReason());
            chatFeign.sendChatMessage("200147","1","有一笔提币进入审核,原因是:"+normal.getReason());
        }
        return new AppResponse().data(id);
    }

    /**
     * 冲提币记录
     * @param coinCode
     * @return
     */
    @RequestMapping("list")
    public AppResponse list(String coinCode,Long timestamp,Integer size,String keyWord,Long startTime,Long endTime,String type){
        if (StringUtils.isBlank(coinCode)) {
            return AppResponse.paramsError();
        }
        if (timestamp == null || timestamp == 0){
            timestamp = System.currentTimeMillis();
        }
        if (size == null || size == 0){
            size = 20;
        }
        List<InWithdrawListVo> all = biz.list(coinCode,timestamp,size,keyWord,startTime,endTime,type);
        AppResponse resp = new AppResponse();
        resp.setData(all);
        return resp;
    }

    @RequestMapping("detail")
    public AppResponse detail(String orderId){
        InWithdrawDetail detail = biz.detail(orderId);
        return AppResponse.ok().data(detail);
    }

    /**
     * 冲提币信息(给前端展示用)
     * 当日
     *
     * @author zkq
     * @date 2018/7/15 16:37
     * @param coinCode
     * @return com.bitcola.exchange.security.common.msg.BaseResponse
     */
    @RequestMapping("withdraw/info")
    public AppResponse withdrawInfo(String coinCode){
        if (StringUtils.isBlank(coinCode)) {
            return AppResponse.paramsError();
        }
        ColaCoin coin = coinBiz.getByCoinCode(coinCode);
        if (TrueFalseConstant.BOOLEAN_NUMBER_FALSE == coin.getIsWithdraw()){
            return AppResponse.error(ResponseCode.COIN_NOT_WITHDRAW_CODE,ResponseCode.COIN_NOT_WITHDRAW_MESSAGE);
        }
        //返回参数有 单笔限额,当日限额,提现费率,固定费率,剩余可提数量,认证方式
        String userID = BaseContextHandler.getUserID();
        Map<String,Object> map =  biz.withdrawInfo(coinCode,userID);
        AppResponse resp = new AppResponse();
        resp.setData(map);
        return resp;
    }

    @Autowired
    IChainServiceFeign chainServiceFeign;

    @RequestMapping("checkAddress")
    public AppResponse checkAddress(String address,String coinCode){
        ColaCoin coin = coinBiz.getByCoinCode(coinCode);
        boolean b = chainServiceFeign.checkAddress(coin.getBelong(), coinCode, address);
        return AppResponse.ok().data(b);
    }

}