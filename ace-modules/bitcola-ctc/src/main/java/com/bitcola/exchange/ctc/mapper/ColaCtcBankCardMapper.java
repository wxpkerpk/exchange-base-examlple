package com.bitcola.exchange.ctc.mapper;


import com.bitcola.ctc.ColaCtcBankCard;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

@Repository
public interface ColaCtcBankCardMapper extends Mapper<ColaCtcBankCard> {
    List<ColaCtcBankCard> list(@Param("userId") String userID);

    List<ColaCtcBankCard> getBusinessList();

    List<Map<String, String>> bankList();

    Map<String, String> getBankInfo(@Param("bankId")String bankId);
}
