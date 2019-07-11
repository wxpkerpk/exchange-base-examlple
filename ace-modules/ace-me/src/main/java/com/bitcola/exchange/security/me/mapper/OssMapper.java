package com.bitcola.exchange.security.me.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OssMapper {

    int updateCoinOss(@Param("img") String img, @Param("id")String id);
    int updateUserOss(@Param("img") String img, @Param("id")String id);
    int updateKycOss(@Param("a") String a,@Param("b") String b,@Param("c") String c, @Param("id")String id);

}
