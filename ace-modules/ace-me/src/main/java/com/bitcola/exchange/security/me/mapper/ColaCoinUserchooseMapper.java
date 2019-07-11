package com.bitcola.exchange.security.me.mapper;

import com.bitcola.me.entity.ColaCoinUserchoose;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 用户自选表
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:17
 */
@Repository
public interface ColaCoinUserchooseMapper extends Mapper<ColaCoinUserchoose> {

    int isExist(@Param("coincode") String coinCode, @Param("symbol") String symbol, @Param("userid") String userID);

    void removeByCoinCode(@Param("coincode") String coinCode, @Param("symbol") String symbol, @Param("userid") String userID);

    void removeById(@Param("id") String id);

    List<ColaCoinUserchoose> list(@Param("userid")String userID);
}
