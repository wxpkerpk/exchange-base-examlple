package com.bitcola.exchange.security.me.mapper;

import com.bitcola.me.entity.ColaMeBalanceWithdrawAddress;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 用户历史提现地址
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-04 17:20:06
 */
@Repository
public interface ColaMeBalanceWithdrawAddressMapper extends Mapper<ColaMeBalanceWithdrawAddress> {

    List<ColaMeBalanceWithdrawAddress> get(@Param("userId") String userID, @Param("coinCode") String coinCode);
}
