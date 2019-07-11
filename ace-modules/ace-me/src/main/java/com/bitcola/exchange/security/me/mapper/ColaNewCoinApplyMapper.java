package com.bitcola.exchange.security.me.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ColaNewCoinApplyMapper {
    void apply(@Param("id") String id,@Param("userId")String userId,@Param("companyName") String companyName, @Param("companyRegisteredAddress")String companyRegisteredAddress,@Param("companyRegisterDate") String companyRegisterDate, @Param("leader")String leader, @Param("telephone")String telephone, @Param("email")String email,@Param("teamSize") String teamSize,@Param("teamInfo") String teamInfo,@Param("projectName") String projectName, @Param("projectWebsite")String projectWebsite,@Param("whitePaper") String whitePaper, @Param("stage")String stage, @Param("projectAlpha")String projectAlpha, @Param("projectInfo")String projectInfo, @Param("coinName")String coinName,@Param("coinCode") String coinCode,@Param("coinBlockChainType") String coinBlockChainType, @Param("coinLiquidityInfo")String coinLiquidityInfo,@Param("coinFeatures") String coinFeatures,@Param("coinRule") String coinRule, @Param("coinPlan")String coinPlan, @Param("privatePlacementInfo")String privatePlacementInfo, @Param("publicPlacementInfo")String publicPlacementInfo,@Param("onlineExchanges") String onlineExchanges,@Param("preOnlineExchanges") String preOnlineExchanges,@Param("communityInfo") String communityInfo, @Param("communityInBitCola")String communityInBitCola,@Param("mediaCoverage") String mediaCoverage,@Param("cooperationPlan") String cooperationPlan,@Param("maintainInfo") String maintainInfo, @Param("promotionInfo")String promotionInfo);


    List<Map<String,String>> applyItem(@Param("userId")String userId);
}
