package com.bitcola.dataservice.mapper;

import com.bitcola.dataservice.dto.WithdrawDto;
import com.bitcola.exchange.security.common.msg.ColaChainOrder;
import com.bitcola.me.entity.ColaMeBalanceWithdrawin;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface ColaChainMapper {

    Map<String, Object> info(@Param("from")String from, @Param("to")String to, @Param("coinCode")String coinCode,@Param("memo")String memo);

    int newRecord(ColaMeBalanceWithdrawin in);

    Map<String, Object> getOne(@Param("txId") String txId);

    int completeDeposit(@Param("userId") String userId, @Param("coinCode")String coinCode, @Param("number")BigDecimal number,@Param("key")String balanceKey);

    Integer confirmNumber(@Param("currentConfirmNumber")Integer currentConfirmNumber, @Param("orderId")String orderId);

    Map<String,Object> selectById(@Param("orderId")String orderId);

    int completeDepositStatus(@Param("orderId")String orderId);

    List<String> getScanAddress(@Param("module")String module);

    List<ColaChainOrder> getExportedOrder(@Param("belong")String belong);

    void withdrawFailed(@Param("orderId")String orderId,@Param("reason")String reason,@Param("key") String withdrawKey);

    void withdrawRollback(@Param("userId")String userId, @Param("number")BigDecimal number, @Param("coinCode")String coinCode, @Param("key")String balanceKey);

    WithdrawDto getWithdrawOrder(@Param("orderId")String orderId);

    int withdrawSuccess(@Param("orderId")String orderId, @Param("hash")String hash, @Param("key")String withdrawKey);

    int withdrawSuccessUnFrozen(@Param("userId")String userId, @Param("number")BigDecimal number, @Param("coinCode")String coinCode, @Param("key")String balanceKey);
}
