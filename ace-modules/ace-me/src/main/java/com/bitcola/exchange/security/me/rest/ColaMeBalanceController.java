package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.constant.ResponseCode;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.rest.BaseController;
import com.bitcola.exchange.security.me.biz.ColaCoinBiz;
import com.bitcola.exchange.security.me.biz.ColaMeBalanceBiz;
import com.bitcola.exchange.security.me.constant.TrueFalseConstant;
import com.bitcola.exchange.security.me.vo.BalanceAddressVo;
import com.bitcola.exchange.security.me.vo.BalanceVo;
import com.bitcola.me.entity.ColaCoin;
import com.bitcola.me.entity.ColaMeBalance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("colaMeBalance")
public class ColaMeBalanceController extends BaseController<ColaMeBalanceBiz,ColaMeBalance> {

    @Autowired
    ColaMeBalanceBiz colaUserBalanceBiz;

    @Autowired
    ColaCoinBiz coinBiz;



    /**
     * 用户资金
     *
     * @author zkq
     * @date 2018/7/14 16:24
     * @return com.bitcola.exchange.security.common.msg.ObjectRestResponse
     */
    @RequestMapping("info")
    public AppResponse info(){
        String userID = BaseContextHandler.getUserID();
        BalanceVo vo = colaUserBalanceBiz.info(userID);
        AppResponse resp = new AppResponse<>();
        resp.setData(vo);
        return resp;
    }

    /**
     * 获取充值地址
     *
     * @author zkq
     * @date 2018/7/14 16:24
     * @return com.bitcola.exchange.security.common.msg.ObjectRestResponse
     */
    @RequestMapping(value = "/getAddress",method = RequestMethod.POST)
    public AppResponse getAddress(@RequestBody Map<String,String> params, HttpServletRequest request){
        String coinCode = params.get("coinCode");
        if (StringUtils.isBlank(coinCode)){
            return AppResponse.paramsError();
        }
        //查看是否可充值
        ColaCoin coin = coinBiz.getByCoinCode(coinCode);
        if (coin.getIsRecharge() == TrueFalseConstant.BOOLEAN_NUMBER_FALSE){
            return new AppResponse(ResponseCode.COIN_NOT_DEPOSIT_CODE, ResponseCode.COIN_NOT_DEPOSIT_MESSAGE);
        }
        BalanceAddressVo obj = colaUserBalanceBiz.getAddress(coinCode);
        return AppResponse.ok().data(obj);
    }

    @RequestMapping(value = "/getColaToken",method = RequestMethod.GET)
    public ColaMeBalance getColaToken(@RequestParam("userId") String userId){
        return baseBiz.getColaToken(userId);
    }



}