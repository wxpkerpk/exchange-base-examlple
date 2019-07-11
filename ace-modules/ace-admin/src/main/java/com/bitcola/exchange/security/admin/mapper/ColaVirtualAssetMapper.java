package com.bitcola.exchange.security.admin.mapper;

import com.bitcola.exchange.security.admin.entity.ColaVirtualAsset;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;

@Repository
public interface ColaVirtualAssetMapper extends Mapper<ColaVirtualAsset> {
    void addVirtualAsset(@Param("amount") BigDecimal amount, @Param("coinCode") String coinCode, @Param("balanceKey")String balanceKey);
}
