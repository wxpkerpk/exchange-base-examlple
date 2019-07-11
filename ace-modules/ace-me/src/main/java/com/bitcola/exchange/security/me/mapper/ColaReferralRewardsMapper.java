package com.bitcola.exchange.security.me.mapper;

import com.bitcola.exchange.security.me.vo.InvitationVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 邀请奖励
 */
@Repository
public interface ColaReferralRewardsMapper {
    String getReferralCode(@Param("userID") String userID);

    Long countInviteFriends(@Param("invitationCode")String invitationCode);

    List<InvitationVo> listInviteFriends(@Param("invitationCode")String invitationCode,@Param("limit")int limit,@Param("page")int page);

    List<Map<String, Object>> referralRewards(@Param("userID")String userID);
}
