package com.bitcola.dataservice.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ColaConfigMapper {

    String getConfig(@Param("config") String config);
}
