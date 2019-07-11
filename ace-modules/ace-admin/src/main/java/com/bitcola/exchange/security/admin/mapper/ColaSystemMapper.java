package com.bitcola.exchange.security.admin.mapper;

import com.bitcola.exchange.security.admin.entity.SystemMaintain;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

@Repository
public interface ColaSystemMapper extends Mapper<SystemMaintain> {
    List<Map<String, Object>> status();

}
