package com.bitcola.exchange.security.me.mapper;


import com.bitcola.me.entity.ColaIcoDeposit;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 充值详情表
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-09-29 14:43:56
 */
public interface ColaIcoDepositMapper extends Mapper<ColaIcoDeposit> {

    List<ColaIcoDeposit> depositList(@Param("userid") String userID);

    String getUserIdByAddress(@Param("address") String address);

    void insertDepositItem(ColaIcoDeposit deposit);

    void updateDepositById(@Param("currentconfirmnumber") Integer currentConfirmNumber, @Param("id") String id, @Param("status")String status);

    Integer idExist(@Param("txid")String txid);
}
