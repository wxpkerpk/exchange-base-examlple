package com.bitcola.exchange.security.me.mapper;


import com.bitcola.me.entity.ColaIcoUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;

/**
 * ico用户信息表
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-09-29 14:43:56
 */
@Repository
public interface ColaIcoUserMapper extends Mapper<ColaIcoUser> {

    ColaIcoUser icoInfo(@Param("userid") String userID);

    Integer icoStatus(@Param("userid")String userID);

    void deleteIcoInfo(@Param("userid")String userID);

    BigDecimal colaTokenNumber(@Param("userid")String userID);

    BigDecimal colaTokenIcoTotalNumber();

    Integer checkAddress(@Param("address")String address, @Param("userid")String userID);

    Integer checkSubscribeExist(@Param("email") String email);

    void subscribe(@Param("id") String id, @Param("email") String email);
}
