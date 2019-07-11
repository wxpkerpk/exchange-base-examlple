package com.bitcola.exchange.security.me.rest;

import com.bitcola.exchange.security.auth.client.i18n.ColaLanguage;
import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.me.biz.ColaNewCoinApplyBiz;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-11-16 18:41
 **/
@RestController
@RequestMapping("newCoin")
public class ColaNewCoinApplyController {

    @Autowired
    ColaNewCoinApplyBiz newCoinApplyBiz;


    @RequestMapping(value = "apply", method = RequestMethod.POST)
    public AppResponse apply(@RequestBody Map<String, String> params) {
        // 第一页
        String companyName = params.get("companyName");
        String companyRegisteredAddress = params.get("companyRegisteredAddress");
        String companyRegisterDate = params.get("companyRegisterDate");
        // 第二页
        String leader = params.get("leader");
        String telephone = params.get("telephone");
        String email = params.get("email");
        String teamSize = params.get("teamSize");
        String teamInfo = params.get("teamInfo");
        // 第三页
        String projectName = params.get("projectName");
        String projectWebsite = params.get("projectWebsite");
        String whitePaper = params.get("whitePaper");
        String stage = params.get("stage");
        String projectAlpha = params.get("projectAlpha");
        String projectInfo = params.get("projectInfo");
        // 第四页
        String coinName = params.get("coinName");
        String coinCode = params.get("coinCode");
        String coinBlockChainType = params.get("coinBlockChainType");
        String coinLiquidityInfo = params.get("coinLiquidityInfo");
        String coinFeatures = params.get("coinFeatures");
        String coinRule = params.get("coinRule");
        String coinPlan = params.get("coinPlan");
        // 第五页
        String privatePlacementInfo = params.get("privatePlacementInfo");
        String publicPlacementInfo = params.get("publicPlacementInfo");
        String onlineExchanges = params.get("onlineExchanges");
        String preOnlineExchanges = params.get("preOnlineExchanges");
        // 第六页
        String communityInfo = params.get("communityInfo");
        String communityInBitCola = params.get("communityInBitCola");
        String mediaCoverage = params.get("mediaCoverage");
        // 第七页
        String cooperationPlan = params.get("cooperationPlan");
        String maintainInfo = params.get("maintainInfo");
        String promotionInfo = params.get("promotionInfo");
        boolean anyBlank = StringUtils.isAnyBlank(companyName, companyRegisteredAddress, companyRegisterDate, leader, telephone, email, teamSize, teamInfo,
                projectName, projectWebsite, stage, projectInfo, coinCode, coinBlockChainType, coinLiquidityInfo, coinFeatures, coinRule, coinPlan,
                privatePlacementInfo, publicPlacementInfo, onlineExchanges, communityInfo, communityInBitCola, cooperationPlan, maintainInfo);
        if (anyBlank) {
            return AppResponse.paramsError();
        }
        List<Map<String, String>> item = newCoinApplyBiz.applyItem();
        if (item != null && item.size() > 0) {
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_NEW_COIN_APPLY_LIMIT));
        }
        try {
            newCoinApplyBiz.apply(companyName, companyRegisteredAddress, companyRegisterDate,
                    leader, telephone, email, teamSize, teamInfo,
                    projectName, projectWebsite, whitePaper, stage, projectAlpha, projectInfo,
                    coinName, coinCode, coinBlockChainType, coinLiquidityInfo,
                    coinFeatures, coinRule, coinPlan,
                    privatePlacementInfo, publicPlacementInfo, onlineExchanges, preOnlineExchanges,
                    communityInfo, communityInBitCola, mediaCoverage,
                    cooperationPlan, maintainInfo, promotionInfo);
        } catch (Exception e){
            e.printStackTrace();
            return AppResponse.error(ColaLanguage.get(ColaLanguage.ME_NEW_COIN_APPLY_LENGTH));
        }
        return AppResponse.ok().data(ColaLanguage.get(ColaLanguage.ME_NEW_COIN_APPLY));
    }


}
