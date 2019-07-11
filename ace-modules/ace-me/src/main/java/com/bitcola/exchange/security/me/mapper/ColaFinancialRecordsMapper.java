package com.bitcola.exchange.security.me.mapper;

import com.bitcola.exchange.security.me.dto.FinancialRecordsDto;
import com.bitcola.exchange.security.me.vo.FinancialRecordsVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ColaFinancialRecordsMapper {
    List<FinancialRecordsVo> list(FinancialRecordsDto dto);

    Integer countList(FinancialRecordsDto dto);

    List<FinancialRecordsVo> cvs(@Param("userId") String userID, @Param("excludeInviteRewards") Integer excludeInviteRewards);

    Map<String, String> detail(@Param("id")String id);

    List<Map<String, Object>> recent(@Param("userID")String userID);
}
