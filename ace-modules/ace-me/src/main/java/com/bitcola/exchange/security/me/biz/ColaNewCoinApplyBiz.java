package com.bitcola.exchange.security.me.biz;

import com.bitcola.exchange.security.common.context.BaseContextHandler;
import com.bitcola.exchange.security.me.mapper.ColaNewCoinApplyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author zkq
 * @create 2018-11-16 19:10
 **/
@Service
public class ColaNewCoinApplyBiz {

    @Autowired
    ColaNewCoinApplyMapper coinApplyMapper;

    public void apply(String companyName, String companyRegisteredAddress, String companyRegisterDate, String leader, String telephone, String email, String teamSize, String teamInfo, String projectName, String projectWebsite, String whitePaper, String stage, String projectAlpha, String projectInfo, String coinName, String coinCode, String coinBlockChainType, String coinLiquidityInfo, String coinFeatures, String coinRule, String coinPlan, String privatePlacementInfo, String publicPlacementInfo, String onlineExchanges, String preOnlineExchanges, String communityInfo, String communityInBitCola, String mediaCoverage, String cooperationPlan, String maintainInfo, String promotionInfo) {
        coinApplyMapper.apply(UUID.randomUUID().toString(), BaseContextHandler.getUserID(), companyName, companyRegisteredAddress, companyRegisterDate,
                leader, telephone, email, teamSize, teamInfo,
                projectName, projectWebsite, whitePaper, stage, projectAlpha, projectInfo,
                coinName, coinCode, coinBlockChainType, coinLiquidityInfo,
                coinFeatures, coinRule, coinPlan,
                privatePlacementInfo, publicPlacementInfo,  onlineExchanges, preOnlineExchanges,
                communityInfo, communityInBitCola, mediaCoverage,
                cooperationPlan, maintainInfo, promotionInfo );
    }

    public List<Map<String,String>> applyItem(){
        return coinApplyMapper.applyItem(BaseContextHandler.getUserID());

    }


}
