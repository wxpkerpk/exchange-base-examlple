package com.bitcola.exchange.security.me.mapper;

 import com.bitcola.me.entity.ColaLoginLog;
 import org.apache.ibatis.annotations.Param;
 import org.springframework.stereotype.Repository;
 import tk.mybatis.mapper.common.Mapper;

 import java.util.List;

/**
 * 登录日志表
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-09-30 16:19:50
 */
@Repository
public interface ColaLoginLogMapper extends Mapper<ColaLoginLog> {
    List<ColaLoginLog> log(@Param("userId") String userId, @Param("page")int page, @Param("limit")int limit);

    Integer countLog(@Param("userId") String userId);

    List<ColaLoginLog> csv(@Param("userId") String userId);
}
