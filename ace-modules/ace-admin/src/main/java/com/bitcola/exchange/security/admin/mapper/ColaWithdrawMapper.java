package com.bitcola.exchange.security.admin.mapper;

import com.bitcola.exchange.security.common.msg.AppResponse;
import com.bitcola.exchange.security.common.util.AdminQuery;
import com.bitcola.me.entity.ColaMeBalanceWithdrawin;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface ColaWithdrawMapper {
    List<ColaMeBalanceWithdrawin> list(AdminQuery query);

    Long total(AdminQuery query);

    Map<String, Object> item(@Param("id") String orderId);

    List<Map<String, Object>> inOut(@Param("userId")String userId);

    int withdrawRefuse(@Param("orderId")String orderId, @Param("reason")String reason, @Param("key")String key);

    BigDecimal withdrawNumber(@Param("orderId")String orderId);

    String withdrawCoinCode(@Param("orderId")String orderId);

    void updateStatus(@Param("orderId") String orderId, @Param("status")String status, @Param("reason")String reason, @Param("key")String key);

    Map<String, String> orderWithdrawInfo(@Param("orderId")String orderId);

    void updateWithdrawStatus(@Param("id")String id, @Param("hash")String hash);

    void updateWithdrawUserBalance(@Param("id")String id, @Param("number")BigDecimal number,@Param("key")String key);
}
