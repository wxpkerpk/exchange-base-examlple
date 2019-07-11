package com.bitcola.exchange.security.me.mapper;

import com.bitcola.exchange.security.me.dto.BalanceDto;
import com.bitcola.me.entity.ColaMeBalance;
import com.bitcola.me.entity.ColaMeBalanceWithdrawin;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户钱包
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:16
 */
@Repository
public interface ColaMeBalanceMapper extends Mapper<ColaMeBalance> {
    /**
     *
     * @param userID
     * @return
     */
    List<BalanceDto> info(@Param("userid") String userID);

    /**
     * 冲提记录
     * @param coinCode
     * @return
     */
    List<ColaMeBalanceWithdrawin> list(@Param("coincode")String coinCode);

    /**
     * 获取用户币种余额
     * @param userID
     * @param coinCode
     * @return
     */
    BigDecimal getCoinNumber(@Param("userid")String userID, @Param("coincode")String coinCode);

    /**
     * 获取当前用户的币种信息
     * @param coinCode
     * @return
     */
    ColaMeBalance selectByCoinCode(@Param("coincode")String coinCode,@Param("userid") String userID);

    ColaMeBalance getColaToken(@Param("userid") String userId);

    void insertWithSign(@Param("entity") ColaMeBalance entity, @Param("key") String balanceKey);

    int withdrawSuccess(@Param("userId")String userID, @Param("number")BigDecimal number, @Param("coinCode")String coinCode, @Param("key")String balanceKey);

    int withdrawFailed(@Param("userId")String userID, @Param("number")BigDecimal number, @Param("coinCode")String coinCode, @Param("key")String balanceKey);

    String selectAddressAndNoteByBelong(@Param("belong")String belong, @Param("userId")String userID);
}
