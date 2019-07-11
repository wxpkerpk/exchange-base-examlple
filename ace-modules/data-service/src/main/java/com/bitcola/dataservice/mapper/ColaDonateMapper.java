package com.bitcola.dataservice.mapper;

import com.bitcola.community.entity.DonateEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * @author zkq
 * @create 2018-10-31 16:23
 **/
@Repository
public interface ColaDonateMapper {

    void insert(DonateEntity entity);
}
