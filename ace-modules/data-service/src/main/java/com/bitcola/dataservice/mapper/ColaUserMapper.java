package com.bitcola.dataservice.mapper;

import com.bitcola.me.entity.ColaUser;
import com.bitcola.me.entity.ColaUserEntity;
import com.bitcola.me.entity.ColaUserKyc;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户表扩展
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-08-01 09:03:17
 */
@Repository
public interface ColaUserMapper extends Mapper<ColaUser> {

    String getMoneyPassword(@Param("id") String id);

    ColaUserEntity info(@Param("userid")String userId);

    List<ColaUserEntity> infoByIds(@Param("userId") ArrayList<String> userId);

    ColaUserEntity infoByInviterCode(@Param("inviterCode")String inviterCode);

    ColaUserKyc getUserKycInfo(@Param("userId")String userId);
}
