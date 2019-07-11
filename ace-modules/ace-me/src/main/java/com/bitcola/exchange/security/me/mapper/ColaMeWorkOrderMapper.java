package com.bitcola.exchange.security.me.mapper;

import com.bitcola.me.entity.ColaMeWorkOrder;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 
 * 
 * @author Mr.AG
 * @email 463540703@qq.com
 * @date 2018-09-12 19:12:17
 */
public interface ColaMeWorkOrderMapper extends Mapper<ColaMeWorkOrder> {

    List<ColaMeWorkOrder> list(@Param("timestamp") Long timestamp, @Param("userid") String userID, @Param("size") Integer size);
}
