package com.bitcola.exchange.security.me.mapper;

import com.bitcola.caculate.entity.ExchangeLog;
import com.bitcola.exchange.security.common.msg.ColaChainBalance;
import com.bitcola.exchange.security.me.dto.WithdrawDto;
import com.bitcola.exchange.security.me.vo.InWithdrawDetail;
import com.bitcola.me.entity.ColaAbnormalEntity;
import com.bitcola.me.entity.ColaMeBalanceWithdrawin;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 用户提现记录
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:16
 */
@Repository
public interface ColaMeBalanceWithdrawinMapper extends Mapper<ColaMeBalanceWithdrawin> {

    /**
     * 冲提记录
     * @param coinCode
     * @param timestamp
     * @param keyWord
     * @return
     */
    List<ColaMeBalanceWithdrawin> list(@Param("coincode") String coinCode, @Param("userid") String userID, @Param("timestamp") Long timestamp, @Param("size") Integer size, @Param("keyWord")String keyWord,@Param("startTime")Long startTime,@Param("endTime")Long endTime,@Param("type")String type);

    /**
     * 今日提现总数
     * @param userID
     * @param coinCode
     * @return
     */
    BigDecimal getTodayNumber(@Param("userid") String userID, @Param("coincode") String coinCode, @Param("date") long date);

    /**
     * 获取当日提现次数
     * @param userID
     * @return
     */
    int getTodayTime(@Param("userid") String userID, @Param("date") long date);

    InWithdrawDetail detail(@Param("orderId")String orderId);

    List<ExchangeLog> recentExchangeLog(@Param("userID")String userID);

    void insertAbnormal(ColaAbnormalEntity abnormalEntity);

    List<Map<String,Object>> listFinancial(@Param("userID")String userID);


    int perWithdraw(@Param("userId") String userID, @Param("number") BigDecimal number, @Param("coinCode") String coinCode,@Param("key") String balanceKey);

    Integer checkBalance(@Param("key") String balanceKey,@Param("userId") String userID);

    int checkSuccess(@Param("id")String id,@Param("status") String status,@Param("key") String withdrawKey);

    List<ColaChainBalance> getAllChainBalance();

    void updateChainBalanceById(ColaChainBalance response);

    ColaChainBalance getChainBalanceByCoinCode(@Param("coinCode") String coinCode);

}
