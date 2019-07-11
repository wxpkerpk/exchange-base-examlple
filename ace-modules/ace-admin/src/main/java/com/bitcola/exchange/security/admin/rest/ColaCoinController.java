package com.bitcola.exchange.security.admin.rest;

import com.bitcola.exchange.security.admin.biz.ColaCoinBiz;
import com.bitcola.exchange.security.admin.feign.IChainServiceFeign;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.msg.TableResultResponse;
import com.bitcola.exchange.security.common.rest.BaseController;
import com.bitcola.exchange.security.common.util.AdminQuery;
import com.bitcola.me.entity.ColaCoin;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * 币种管理 (上币,)
 *
 * @author zkq
 * @create 2018-10-30 18:59
 **/
@RestController
@RequestMapping("cola/coin")
public class ColaCoinController extends BaseController<ColaCoinBiz, ColaCoin> {

    @Autowired
    IChainServiceFeign chainServiceFeign;

    @RequestMapping("coinApply")
    public TableResultResponse coinApply(@RequestParam Map<String, Object> params){
        AdminQuery query = new AdminQuery(params);
        return baseBiz.coinApply(query);
    }


    /**
     * 代币资料(EOS)
     * @return
     */
    @RequestMapping(value = "insertCoinEosToken",method = RequestMethod.POST)
    public AppResponse insertCoinEosToken(@RequestBody Map<String, Object> params){
        String coinCode = params.get("coinCode").toString();
        String tokenName = params.get("tokenName").toString();
        String symbol = params.get("symbol").toString();
        Integer precision = Integer.valueOf(params.get("precision").toString());
        if (StringUtils.isAnyBlank(coinCode,tokenName,symbol)){
            return AppResponse.error("参数错了,请检查");
        }
        baseBiz.insertCoinEosToken(coinCode,tokenName,symbol,precision);
        return AppResponse.ok();
    }
    /**
     * 代币资料(ETH)
     * @return
     */
    @RequestMapping(value = "insertCoinEthToken",method = RequestMethod.POST)
    public AppResponse insertCoinEthToken(@RequestBody Map<String, Object> params){
        String coinCode = params.get("coinCode").toString();
        String contract = params.get("contract").toString();
        String minAutoTransferToHot = params.get("minAutoTransferToHot").toString();
        BigDecimal autoToHotNumber = new BigDecimal(minAutoTransferToHot);
        if (StringUtils.isAnyBlank(coinCode,contract,minAutoTransferToHot)){
            return AppResponse.error("参数错了,请检查");
        }
        baseBiz.insertCoinEthToken(coinCode,contract,autoToHotNumber);
        return AppResponse.ok();
    }

    @RequestMapping("eosTokenList")
    public AppResponse eosTokenList(){
        List<Map<String,Object>> list = baseBiz.eosTokenList();
        return AppResponse.ok().data(list);
    }
    @RequestMapping("ethTokenList")
    public AppResponse ethTokenList(){
        List<Map<String,Object>> list = baseBiz.ethTokenList();
        return AppResponse.ok().data(list);
    }

    @RequestMapping(value = "insertCoinXlmToken",method = RequestMethod.POST)
    public AppResponse insertCoinXlmToken(@RequestBody Map<String, Object> params){
        String coinCode = params.get("coinCode").toString();
        String tokenCode = params.get("tokenCode").toString();
        String tokenIssuer = params.get("tokenIssuer").toString();
        if (StringUtils.isAnyBlank(coinCode,tokenCode,tokenIssuer)){
            return AppResponse.error("参数错了,请检查");
        }
        baseBiz.insertCoinXlmToken(coinCode,tokenCode,tokenIssuer);
        String hash = chainServiceFeign.trustToken(tokenCode, tokenIssuer);
        return AppResponse.ok().data("添加 XLM 代币成功:"+hash);
    }

    @RequestMapping("xlmTokenList")
    public AppResponse xlmTokenList(){
        List<Map<String,Object>> list = baseBiz.xlmTokenList();
        return AppResponse.ok().data(list);
    }


}
