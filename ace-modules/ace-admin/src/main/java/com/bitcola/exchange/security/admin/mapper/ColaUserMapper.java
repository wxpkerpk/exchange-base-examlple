package com.bitcola.exchange.security.admin.mapper;

import com.bitcola.exchange.security.admin.entity.SysUserEntity;
import com.bitcola.exchange.security.admin.vo.UserAddress;
import com.bitcola.exchange.security.common.util.AdminQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author zkq
 * @create 2018-11-27 12:09
 **/
@Repository
public interface ColaUserMapper {
    List<SysUserEntity> list(AdminQuery query);
    Long total(AdminQuery query);
    Map<String, Object> info(@Param("id") String id);
    List<Map<String, Object>> balance(@Param("id")String id);
    List<Map<String, Object>> transaction(@Param("id")String id);

    void updateBaseUser(@Param("id")String id, @Param("username")String username, @Param("email")String email, @Param("telephone")String telephone, @Param("areaCode")String areaCode);

    void updateColaUser(@Param("id")String id, @Param("enable")Integer enable);

    void resetPin(@Param("id")String id);

    Integer repeat(@Param("username")String username, @Param("email")String email, @Param("areaCode")String areaCode, @Param("telephone")String telephone);

    List<SysUserEntity> inviterList(AdminQuery query);

    Long inviterCount(AdminQuery query);

    void insertCustomer(@Param("id")String id, @Param("nickName")String nickName, @Param("avatar")String defaultAvatar, @Param("timestamp")Long currentTimeMillis);

    List<UserAddress> userAddressList(AdminQuery query);


    Long userAddressCount(AdminQuery query);
}
