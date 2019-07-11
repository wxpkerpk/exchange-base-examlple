package com.bitcola.exchange.security.admin.rest;

import com.bitcola.exchange.security.admin.biz.ColaFinancialBiz;
import com.bitcola.exchange.security.common.constant.UserConstant;
import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.util.AdminQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 资产管理,包含发放奖励 (奖励发放,资金统计)
 *
 * @author zkq
 * @create 2018-10-30 19:02
 **/
@RestController
@RequestMapping("cola/financial")
public class ColaFinancialController {

    @Autowired
    ColaFinancialBiz biz;

    @RequestMapping(value = "reward",method = RequestMethod.POST)
    public AppResponse reward(@RequestBody Map<String,String> params){
        String coinCode = params.get("coinCode");
        String number = params.get("number");
        String userId = params.get("userId");
        String description = params.get("description");
        if (StringUtils.isAnyBlank(coinCode,number,userId)){
            return AppResponse.paramsError();
        }
        if (!UserConstant.SYS_ACCOUNT_ID.equals(BaseContextHandler.getUserID())){
            return AppResponse.error("您没有权限");
        }
        BigDecimal amount = new BigDecimal(number);
        if (amount.compareTo(BigDecimal.ZERO)<0){
            return AppResponse.error("您输入的数量不正确");
        }
        boolean reward = biz.reward(coinCode, amount, userId, description);
        if (!reward) return AppResponse.error("出现错误,请检查用户资金情况 : "+userId);
        return AppResponse.ok();

    }


    @RequestMapping(value = "frozen",method = RequestMethod.POST)
    public AppResponse frozen(@RequestBody Map<String,String> params){
        String coinCode = params.get("coinCode");
        String number = params.get("number");
        String userId = params.get("userId");
        String description = params.get("description");
        if (StringUtils.isAnyBlank(coinCode,number,userId)){
            return AppResponse.paramsError();
        }
        if (!UserConstant.SYS_ACCOUNT_ID.equals(BaseContextHandler.getUserID())){
            return AppResponse.error("您没有权限");
        }
        BigDecimal amount = new BigDecimal(number);
        if (amount.compareTo(BigDecimal.ZERO)<0){
            return AppResponse.error("您输入的数量不正确");
        }
        boolean reward = biz.freeze(coinCode, amount, userId, description);
        if (!reward) return AppResponse.error("出现错误,请检查用户资金情况 : "+userId);
        return AppResponse.ok();

    }
    @RequestMapping(value = "unFrozen",method = RequestMethod.POST)
    public AppResponse unFrozen(@RequestBody Map<String,String> params){
        String coinCode = params.get("coinCode");
        String number = params.get("number");
        String userId = params.get("userId");
        String description = params.get("description");
        if (StringUtils.isAnyBlank(coinCode,number,userId)){
            return AppResponse.paramsError();
        }
        if (!UserConstant.SYS_ACCOUNT_ID.equals(BaseContextHandler.getUserID())){
            return AppResponse.error("您没有权限");
        }
        BigDecimal amount = new BigDecimal(number);
        if (amount.compareTo(BigDecimal.ZERO)<0){
            return AppResponse.error("您输入的数量不正确");
        }
        boolean reward = biz.unFrozen(coinCode, amount, userId, description);
        if (!reward) return AppResponse.error("出现错误,请检查用户资金情况 : "+userId);
        return AppResponse.ok();

    }
    @RequestMapping(value = "reduce",method = RequestMethod.POST)
    public AppResponse reduce(@RequestBody Map<String,String> params){
        String coinCode = params.get("coinCode");
        String number = params.get("number");
        String userId = params.get("userId");
        String description = params.get("description");
        if (StringUtils.isAnyBlank(coinCode,number,userId)){
            return AppResponse.paramsError();
        }
        if (!UserConstant.SYS_ACCOUNT_ID.equals(BaseContextHandler.getUserID())){
            return AppResponse.error("您没有权限");
        }
        BigDecimal amount = new BigDecimal(number);
        if (amount.compareTo(BigDecimal.ZERO)<0){
            return AppResponse.error("您输入的数量不正确");
        }
        boolean reward = biz.reduce(coinCode, amount, userId, description);
        if (!reward) return AppResponse.error("出现错误,请检查用户资金情况 : "+userId);
        return AppResponse.ok();

    }


    /**
     * 添加虚拟资产
     * @param params
     * @return
     */
    @RequestMapping(value = "addVirtualAsset",method = RequestMethod.POST)
    public AppResponse addVirtualAsset(@RequestBody Map<String,String> params){
        String coinCode = params.get("coinCode");
        String number = params.get("number");
        String description = params.get("description");
        if (StringUtils.isAnyBlank(coinCode,number,description)){
            return AppResponse.paramsError();
        }
        if (!UserConstant.SYS_ACCOUNT_ID.equals(BaseContextHandler.getUserID())){
            return AppResponse.error("您没有权限");
        }
        biz.addVirtualAsset(coinCode,number,description);
        return AppResponse.ok();

    }



    @RequestMapping(value = "page",method = RequestMethod.GET)
    public TableResultResponse page(@RequestParam Map<String, Object> params){
        AdminQuery query = new AdminQuery(params);
        return biz.page(query);
    }

    @RequestMapping(value = "coinRange",method = RequestMethod.GET)
    public TableResultResponse coinRange(@RequestParam Map<String, Object> params){
        AdminQuery query = new AdminQuery(params);
        return biz.coinRange(query);
    }

    @RequestMapping(value = "financialPage",method = RequestMethod.GET)
    public TableResultResponse financialPage(@RequestParam Map<String, Object> params){
        AdminQuery query = new AdminQuery(params);
        return biz.financialPage(query);
    }



}
